package com.northstar.crm.api;

import com.northstar.crm.dto.CustomerRequestDTO;
import com.northstar.crm.dto.CustomerResponseDTO;
import com.northstar.crm.entity.Customer;
import com.northstar.crm.entity.CustomerStatus;
import com.northstar.crm.mapper.CustomerMapper;
import com.northstar.crm.service.CustomerService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The API edge. Order is validate, map, call the service, map back, and nothing
 * here returns a Customer. The field is the CustomerService interface, not an
 * implementation.
 *
 * Every failure carries a stable code and the caller's correlation id. Rules
 * below the facade throw IllegalArgumentException and IllegalStateException;
 * the codes are attached here.
 */
public class CustomerApiFacade {

    private static final System.Logger LOG = System.getLogger(CustomerApiFacade.class.getName());

    static final String CODE_VALIDATION_FAILED = "CUSTOMER_VALIDATION_FAILED";
    static final String CODE_STATUS_INVALID = "CUSTOMER_STATUS_INVALID";
    static final String CODE_NOT_FOUND = "CUSTOMER_NOT_FOUND";
    static final String CODE_CONFLICT = "CUSTOMER_CONFLICT";
    static final String CODE_TRANSITION_INVALID = "CUSTOMER_TRANSITION_INVALID";

    private final CustomerService service;
    private final Validator validator;

    public CustomerApiFacade(CustomerService service) {
        this.service = service;
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    public CustomerResponseDTO create(CustomerRequestDTO request, String correlationId) {
        validateOrThrow(request, correlationId);
        Customer entity = toEntityOrThrow(request, correlationId);
        try {
            return CustomerMapper.toResponse(service.addCustomer(entity));
        } catch (IllegalStateException ex) {
            // duplicate id or email, a legal payload that conflicts with the store
            throw new IllegalStateException(fail(correlationId, CODE_CONFLICT, ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    fail(correlationId, CODE_VALIDATION_FAILED, ex.getMessage()));
        }
    }

    public CustomerResponseDTO get(String customerId, String correlationId) {
        Customer entity = service.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        fail(correlationId, CODE_NOT_FOUND, "customerId: " + customerId)));
        return CustomerMapper.toResponse(entity);
    }

    /**
     * Status arrives as a String, so the enum lookup can fail here the same way
     * it does on create.
     */
    public CustomerResponseDTO changeStatus(String customerId, String status, String correlationId) {
        CustomerStatus target;
        try {
            target = CustomerStatus.valueOf(status);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new IllegalArgumentException(fail(correlationId, CODE_STATUS_INVALID,
                    "status: " + status + " is not a known status"));
        }
        try {
            return CustomerMapper.toResponse(service.changeStatus(customerId, target, correlationId));
        } catch (IllegalStateException ex) {
            throw new IllegalStateException(
                    fail(correlationId, CODE_TRANSITION_INVALID, ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(fail(correlationId, CODE_NOT_FOUND, ex.getMessage()));
        }
    }

    private void validateOrThrow(CustomerRequestDTO request, String correlationId) {
        if (request == null) {
            throw new IllegalArgumentException(
                    fail(correlationId, CODE_VALIDATION_FAILED, "request: must be provided"));
        }
        Set<ConstraintViolation<CustomerRequestDTO>> violations = validator.validate(request);
        if (violations.isEmpty()) {
            return;
        }
        // validate() returns a Set, so sort before joining or the field order
        // moves between runs.
        String detail = violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.joining("; "));
        throw new IllegalArgumentException(fail(correlationId, CODE_VALIDATION_FAILED, detail));
    }

    /**
     * status has no value constraint, so a typo survives validation and dies in
     * CustomerStatus.valueOf. Translated here so the JDK's enum message does not
     * reach the client.
     */
    private Customer toEntityOrThrow(CustomerRequestDTO request, String correlationId) {
        try {
            return CustomerMapper.toEntity(request);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    fail(correlationId, CODE_STATUS_INVALID,
                            "status: " + request.getStatus() + " is not a known status"));
        }
    }

    /* Builds the failure message and logs the same string that is thrown. */
    private static String fail(String correlationId, String code, String detail) {
        String message = "[" + correlationId + "] " + code + ": " + detail;
        LOG.log(System.Logger.Level.WARNING, message);
        return message;
    }
}
