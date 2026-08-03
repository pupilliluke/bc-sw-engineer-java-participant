package com.northstar.crm.api;

import com.northstar.crm.dto.CustomerRequestDTO;
import com.northstar.crm.entity.Customer;
import com.northstar.crm.entity.CustomerStatus;
import com.northstar.crm.exception.BusinessException;
import com.northstar.crm.exception.ErrorResponse;
import com.northstar.crm.exception.GlobalExceptionHandler;
import com.northstar.crm.mapper.CustomerMapper;
import com.northstar.crm.service.CustomerService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Map;
import java.util.Set;

/**
 * The API edge. Order is validate, map, call the service, map back, and nothing
 * here returns a Customer.
 *
 * Lab 15 threw on failure and every caller wrote a try/catch. This returns
 * ApiResult, so a failure is an ErrorResponse the caller can print or serialise
 * without catching anything. BusinessException is caught before Exception in
 * every method; the other order turns a 404 into a 500.
 */
public class CustomerApiFacade {

    private final CustomerService service;
    private final GlobalExceptionHandler handler;
    private final Validator validator;

    public CustomerApiFacade(CustomerService service) {
        this(service, new GlobalExceptionHandler());
    }

    public CustomerApiFacade(CustomerService service, GlobalExceptionHandler handler) {
        this.service = service;
        this.handler = handler;
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    public ApiResult create(CustomerRequestDTO request, String correlationId) {
        requireCorrelationId(correlationId);
        if (request == null) {
            return fail(handler.fromFields(Map.of("request", "must be provided"), correlationId));
        }
        Set<ConstraintViolation<CustomerRequestDTO>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            return fail(handler.fromValidation(violations, correlationId));
        }
        Customer entity;
        try {
            entity = CustomerMapper.toEntity(request);
        } catch (IllegalArgumentException ex) {
            // status has no value constraint, so a typo survives Bean Validation
            // and dies in CustomerStatus.valueOf. Translated here so the JDK's
            // enum message does not reach the client.
            return fail(handler.fromFields(
                    Map.of("status", request.getStatus() + " is not a known status"), correlationId));
        }
        try {
            return new ApiResult.Ok(CustomerMapper.toResponse(service.addCustomer(entity, correlationId)));
        } catch (BusinessException ex) {
            return fail(handler.fromBusiness(ex));
        } catch (Exception ex) {
            return fail(handler.fromUnexpected(ex, correlationId));
        }
    }

    public ApiResult getById(String customerId, String correlationId) {
        requireCorrelationId(correlationId);
        try {
            Customer entity = service.findById(customerId)
                    .orElseThrow(() -> BusinessException.notFound(customerId, correlationId));
            return new ApiResult.Ok(CustomerMapper.toResponse(entity));
        } catch (BusinessException ex) {
            return fail(handler.fromBusiness(ex));
        } catch (Exception ex) {
            return fail(handler.fromUnexpected(ex, correlationId));
        }
    }

    public ApiResult changeStatus(String customerId, String status, String correlationId) {
        requireCorrelationId(correlationId);
        CustomerStatus target;
        try {
            target = CustomerStatus.valueOf(status);
        } catch (IllegalArgumentException | NullPointerException ex) {
            return fail(handler.fromFields(
                    Map.of("status", status + " is not a known status"), correlationId));
        }
        try {
            return new ApiResult.Ok(
                    CustomerMapper.toResponse(service.changeStatus(customerId, target, correlationId)));
        } catch (BusinessException ex) {
            return fail(handler.fromBusiness(ex));
        } catch (Exception ex) {
            return fail(handler.fromUnexpected(ex, correlationId));
        }
    }

    /**
     * Thrown rather than returned. Without an id there is nothing to put in the
     * ErrorResponse, and a caller that omits it has a bug rather than a bad
     * request.
     */
    private static void requireCorrelationId(String correlationId) {
        if (correlationId == null || correlationId.isBlank()) {
            throw new IllegalArgumentException("correlationId is required at the API edge");
        }
    }

    private static ApiResult fail(ErrorResponse error) {
        return new ApiResult.Fail(error);
    }
}
