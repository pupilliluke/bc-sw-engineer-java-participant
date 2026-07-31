package com.northstar.crm.exception;

import com.northstar.crm.dto.CustomerRequestDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerTest {

    private static final String CORRELATION_ID = "lab-request-001";

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsNotFoundTo404() {
        ErrorResponse error = handler.fromBusiness(
                BusinessException.notFound("CUS-9999", CORRELATION_ID));

        assertEquals(404, error.getStatus());
        assertEquals("CUSTOMER_NOT_FOUND", error.getError());
        assertEquals(CORRELATION_ID, error.getCorrelationId());
        assertTrue(error.getMessage().contains("CUS-9999"), error.getMessage());
    }

    @Test
    void mapsConflictTo409() {
        ErrorResponse error = handler.fromBusiness(BusinessException.conflict(
                "illegal status transition ACTIVE -> PROSPECT", CORRELATION_ID));

        assertEquals(409, error.getStatus());
        assertEquals("BUSINESS_CONFLICT", error.getError());
        assertTrue(error.getErrors().isEmpty(), "a conflict has no field to blame");
    }

    @Test
    void mapsValidationTo400WithTheFailingField() {
        ErrorResponse error = handler.fromValidation(violationsFor(badEmail()), CORRELATION_ID);

        assertEquals(400, error.getStatus());
        assertEquals("VALIDATION_FAILED", error.getError());
        assertEquals("email must be a valid address", error.getErrors().get("email"));
        assertEquals(CORRELATION_ID, error.getCorrelationId());
    }

    @Test
    void everyFailingFieldIsReportedAtOnce() {
        CustomerRequestDTO request = badEmail();
        request.setFullName(" ");

        ErrorResponse error = handler.fromValidation(violationsFor(request), CORRELATION_ID);

        assertTrue(error.getErrors().containsKey("email"), error.toJson());
        assertTrue(error.getErrors().containsKey("fullName"), error.toJson());
    }

    @Test
    void unexpectedIsGeneric500() {
        Exception cause = new IllegalStateException(
                "connection to jdbc:postgresql://crm-db/customers refused");

        ErrorResponse error = handler.fromUnexpected(cause, CORRELATION_ID);

        assertEquals(500, error.getStatus());
        assertEquals("INTERNAL_ERROR", error.getError());
        assertEquals("Unexpected server error", error.getMessage());
        assertFalse(error.toJson().contains("jdbc"), "the client payload leaked the cause");
        assertFalse(error.toJson().contains("IllegalStateException"), error.toJson());
        assertEquals(CORRELATION_ID, error.getCorrelationId());
    }

    @Test
    void everyMappingCarriesTheCorrelationId() {
        assertEquals(CORRELATION_ID,
                handler.fromBusiness(BusinessException.notFound("CUS-9999", CORRELATION_ID))
                        .getCorrelationId());
        assertEquals(CORRELATION_ID,
                handler.fromValidation(violationsFor(badEmail()), CORRELATION_ID).getCorrelationId());
        assertEquals(CORRELATION_ID,
                handler.fromUnexpected(new RuntimeException("boom"), CORRELATION_ID)
                        .getCorrelationId());
    }

    private static Set<ConstraintViolation<CustomerRequestDTO>> violationsFor(
            CustomerRequestDTO request) {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            return validator.validate(request);
        }
    }

    private static CustomerRequestDTO badEmail() {
        return new CustomerRequestDTO("CUS-1001", "Amina Khan",
                "not-an-email", "555-0101", "ACTIVE");
    }
}
