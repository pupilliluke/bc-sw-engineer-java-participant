package com.northstar.crm.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErrorResponseTest {

    private static final Instant FIXED = Instant.parse("2026-07-31T12:00:00Z");
    private static final String CORRELATION_ID = "lab-request-001";

    @Test
    void jsonAlwaysCarriesAnErrorsObject() {
        ErrorResponse error = new ErrorResponse(FIXED, 404, "CUSTOMER_NOT_FOUND",
                "Customer not found: CUS-9999", CORRELATION_ID, null);

        assertEquals("{\"timestamp\":\"2026-07-31T12:00:00Z\",\"status\":404,"
                + "\"error\":\"CUSTOMER_NOT_FOUND\","
                + "\"message\":\"Customer not found: CUS-9999\","
                + "\"correlationId\":\"lab-request-001\",\"errors\":{}}", error.toJson());
    }

    @Test
    void fieldErrorsKeepTheOrderTheyWereCollectedIn() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("email", "email must be a valid address");
        fields.put("fullName", "fullName is required");

        String json = new ErrorResponse(FIXED, 400, "VALIDATION_FAILED",
                "Validation failed", CORRELATION_ID, fields).toJson();

        assertTrue(json.endsWith("\"errors\":{\"email\":\"email must be a valid address\","
                + "\"fullName\":\"fullName is required\"}}"), json);
    }

    @Test
    void aQuoteInAMessageDoesNotBreakTheDocument() {
        ErrorResponse error = new ErrorResponse(FIXED, 400, "VALIDATION_FAILED",
                "fullName \"O\"Brien\" is rejected\nsecond line", CORRELATION_ID, Map.of());

        assertTrue(error.toJson().contains(
                "\\\"O\\\"Brien\\\" is rejected\\nsecond line"), error.toJson());
    }

    @Test
    void errorsCannotBeMutatedByTheCaller() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("email", "email must be a valid address");
        ErrorResponse error = new ErrorResponse(FIXED, 400, "VALIDATION_FAILED",
                "Validation failed", CORRELATION_ID, fields);

        fields.put("phone", "added after construction");

        assertEquals(1, error.getErrors().size());
        assertThrows(UnsupportedOperationException.class,
                () -> error.getErrors().put("phone", "added through the getter"));
    }
}
