package com.northstar.crm.service;

import com.northstar.crm.entity.CustomerStatus;
import com.northstar.crm.exception.BusinessException;
import com.northstar.crm.repository.InMemoryCustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Lab 15 ALLOWED map as a table. Every row here has a matching entry in
 * CustomerValidator.ALLOWED, so a row that stops matching is a rule change and
 * not a test to relax.
 *
 * Jupiter converts the CSV strings to CustomerStatus by constant name, so a
 * typo in a row fails as an argument conversion error rather than passing.
 */
class CustomerValidatorParameterizedTest {

    private static final String CORRELATION_ID = "lab-request-001";

    private CustomerValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CustomerValidator(new InMemoryCustomerRepository());
    }

    @ParameterizedTest(name = "{0} -> {1} is allowed")
    @CsvSource({
            "PROSPECT, ACTIVE",
            "PROSPECT, CLOSED",
            "ACTIVE, SUSPENDED",
            "ACTIVE, CLOSED",
            "SUSPENDED, ACTIVE",
            "SUSPENDED, CLOSED"
    })
    void legalTransitionIsAccepted(CustomerStatus from, CustomerStatus to) {
        assertDoesNotThrow(() -> validator.validateTransition(from, to, CORRELATION_ID));
    }

    @ParameterizedTest(name = "{0} -> {1} is a 409")
    @CsvSource({
            "ACTIVE, PROSPECT",
            "CLOSED, ACTIVE",
            "CLOSED, PROSPECT",
            "CLOSED, SUSPENDED",
            "PROSPECT, SUSPENDED",
            "SUSPENDED, PROSPECT",
            "ACTIVE, ACTIVE",
            "PROSPECT, PROSPECT",
            "SUSPENDED, SUSPENDED"
    })
    void illegalTransitionIsRejectedAsAConflict(CustomerStatus from, CustomerStatus to) {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validateTransition(from, to, CORRELATION_ID));

        assertEquals(409, ex.getStatusHint());
        assertEquals("BUSINESS_CONFLICT", ex.getCode());
        assertTrue(ex.getMessage().contains(from + " -> " + to), ex.getMessage());
        assertEquals(CORRELATION_ID, ex.getCorrelationId(),
                "support cannot trace the report without the correlation id");
    }

    @ParameterizedTest(name = "CLOSED -> {0} stays terminal")
    @EnumSource(CustomerStatus.class)
    void closedAcceptsNoTarget(CustomerStatus to) {
        assertThrows(BusinessException.class,
                () -> validator.validateTransition(CustomerStatus.CLOSED, to, CORRELATION_ID));
    }

    @ParameterizedTest(name = "{0} -> null is a 400")
    @EnumSource(CustomerStatus.class)
    void aMissingTargetIsAValidationFailureFromEveryStatus(CustomerStatus from) {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validateTransition(from, null, CORRELATION_ID));

        assertEquals(400, ex.getStatusHint());
        assertEquals("VALIDATION_FAILED", ex.getCode());
        assertTrue(ex.getMessage().contains("newStatus"), ex.getMessage());
    }
}
