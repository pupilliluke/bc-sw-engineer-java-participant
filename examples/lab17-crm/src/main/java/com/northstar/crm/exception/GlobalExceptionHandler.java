package com.northstar.crm.exception;

import jakarta.validation.ConstraintViolation;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * One place decides status, code and client message. Nothing else in the
 * project builds an ErrorResponse.
 *
 * Three families in, one shape out: a typed domain failure, a set of Bean
 * Validation violations, and everything that was not expected. Spring's
 * {@code @ControllerAdvice} replaces the call sites in Week 3; these three
 * methods are the mapping it will delegate to.
 */
public class GlobalExceptionHandler {

    private static final System.Logger LOG =
            System.getLogger(GlobalExceptionHandler.class.getName());

    public ErrorResponse fromBusiness(BusinessException ex) {
        LOG.log(System.Logger.Level.WARNING, "[" + ex.getCorrelationId() + "] "
                + ex.getCode() + ": " + ex.getMessage());
        return new ErrorResponse(ex.getStatusHint(), ex.getCode(), ex.getMessage(),
                ex.getCorrelationId(), Map.of());
    }

    public ErrorResponse fromValidation(
            Set<? extends ConstraintViolation<?>> violations, String correlationId) {
        Map<String, String> fields = new LinkedHashMap<>();
        // validate() returns a Set, so sort before collecting or the field
        // order moves between runs.
        violations.stream()
                .sorted(Comparator.comparing((ConstraintViolation<?> v) -> v.getPropertyPath().toString())
                        .thenComparing(ConstraintViolation::getMessage))
                .forEach(v -> fields.putIfAbsent(v.getPropertyPath().toString(), v.getMessage()));
        return fromFields(fields, correlationId);
    }

    /** The same 400 for a failure that has a field but no ConstraintViolation. */
    public ErrorResponse fromFields(Map<String, String> fields, String correlationId) {
        LOG.log(System.Logger.Level.WARNING, "[" + correlationId + "] "
                + BusinessException.CODE_VALIDATION_FAILED + ": " + fields.keySet());
        return new ErrorResponse(400, BusinessException.CODE_VALIDATION_FAILED,
                "Validation failed", correlationId, fields);
    }

    /**
     * The stack trace is logged and stays here. The client gets a fixed
     * sentence, because ex.getMessage() on an unexpected failure can carry a
     * file path, a query or an address.
     */
    public ErrorResponse fromUnexpected(Exception ex, String correlationId) {
        LOG.log(System.Logger.Level.ERROR, "[" + correlationId + "] INTERNAL_ERROR", ex);
        return new ErrorResponse(500, "INTERNAL_ERROR",
                "Unexpected server error", correlationId, Map.of());
    }
}
