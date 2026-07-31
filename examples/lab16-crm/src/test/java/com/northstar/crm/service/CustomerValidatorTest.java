package com.northstar.crm.service;

import com.northstar.crm.entity.Customer;
import com.northstar.crm.entity.CustomerStatus;
import com.northstar.crm.exception.BusinessException;
import com.northstar.crm.repository.InMemoryCustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerValidatorTest {

    private static final String CORRELATION_ID = "lab-request-001";

    private InMemoryCustomerRepository repo;
    private CustomerValidator validator;

    @BeforeEach
    void setUp() {
        repo = new InMemoryCustomerRepository();
        validator = new CustomerValidator(repo);
    }

    @Test
    void prospectToActiveIsAllowed() {
        assertDoesNotThrow(() -> validator.validateTransition(
                CustomerStatus.PROSPECT, CustomerStatus.ACTIVE, CORRELATION_ID));
    }

    @Test
    void activeToProspectIsRejectedAsAConflict() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validateTransition(
                        CustomerStatus.ACTIVE, CustomerStatus.PROSPECT, CORRELATION_ID));

        assertEquals(409, ex.getStatusHint());
        assertEquals("BUSINESS_CONFLICT", ex.getCode());
        assertTrue(ex.getMessage().contains("ACTIVE -> PROSPECT"), ex.getMessage());
        assertEquals(CORRELATION_ID, ex.getCorrelationId(),
                "support cannot trace the report without the correlation id");
    }

    @Test
    void sameStatusIsRejectedRatherThanIgnored() {
        assertThrows(BusinessException.class, () -> validator.validateTransition(
                CustomerStatus.ACTIVE, CustomerStatus.ACTIVE, CORRELATION_ID));
    }

    @Test
    void closedIsTerminal() {
        assertThrows(BusinessException.class, () -> validator.validateTransition(
                CustomerStatus.CLOSED, CustomerStatus.ACTIVE, CORRELATION_ID));
    }

    @Test
    void duplicateIdIsAConflict() {
        repo.save(amina());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validateNew(new Customer("CUS-1001", "Someone Else",
                        "someone@example.com", null, CustomerStatus.PROSPECT, null), CORRELATION_ID));

        assertEquals(409, ex.getStatusHint());
    }

    @Test
    void duplicateEmailIsRejectedIgnoringCase() {
        repo.save(amina());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validateNew(new Customer("CUS-1009", "Amina K",
                        "AMINA.KHAN@example.com", null, CustomerStatus.PROSPECT, null), CORRELATION_ID));

        assertEquals(409, ex.getStatusHint());
        assertTrue(ex.getMessage().contains("already registered"), ex.getMessage());
        assertFalse(ex.getMessage().contains("AMINA.KHAN"),
                "the client message must not echo the address back");
    }

    @Test
    void blankCustomerIdIsA400NotAConflict() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validateNew(new Customer("   ", "Amina Khan",
                        "amina.khan@example.com", null, CustomerStatus.ACTIVE, null), CORRELATION_ID));

        assertEquals(400, ex.getStatusHint());
        assertEquals("VALIDATION_FAILED", ex.getCode());
    }

    private static Customer amina() {
        return new Customer("CUS-1001", "Amina Khan", "amina.khan@example.com",
                "555-0101", CustomerStatus.ACTIVE, null);
    }
}
