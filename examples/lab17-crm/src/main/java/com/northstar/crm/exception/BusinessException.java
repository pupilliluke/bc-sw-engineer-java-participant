package com.northstar.crm.exception;

/**
 * The domain failure type. Lab 15 threw IllegalStateException for both a
 * duplicate email and a repository outage, and the facade could not tell them
 * apart; a code and a status hint decided at the throw site can.
 *
 * The correlation id travels on the exception, so a failure raised three calls
 * below the facade still reaches the client with the id the caller sent.
 */
public class BusinessException extends RuntimeException {

    public static final String CODE_NOT_FOUND = "CUSTOMER_NOT_FOUND";
    public static final String CODE_CONFLICT = "BUSINESS_CONFLICT";
    public static final String CODE_VALIDATION_FAILED = "VALIDATION_FAILED";

    private final String code;
    private final int statusHint;
    private final String correlationId;

    public BusinessException(String code, String message, int statusHint, String correlationId) {
        super(message);
        this.code = code;
        this.statusHint = statusHint;
        this.correlationId = correlationId;
    }

    public String getCode() {
        return code;
    }

    public int getStatusHint() {
        return statusHint;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public static BusinessException notFound(String customerId, String correlationId) {
        return new BusinessException(CODE_NOT_FOUND,
                "Customer not found: " + customerId, 404, correlationId);
    }

    public static BusinessException conflict(String message, String correlationId) {
        return new BusinessException(CODE_CONFLICT, message, 409, correlationId);
    }

    /**
     * The service-layer backstop for a payload that never reached Bean
     * Validation. Callers coming through the facade fail earlier than this.
     */
    public static BusinessException validation(String field, String message, String correlationId) {
        return new BusinessException(CODE_VALIDATION_FAILED,
                field + ": " + message, 400, correlationId);
    }
}
