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

    @Test
    void blankFullNameIsA400NamingTheField() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validateNew(new Customer("CUS-1007", "   ",
                        "blank.name@example.com", null, CustomerStatus.PROSPECT, null), CORRELATION_ID));

        assertEquals(400, ex.getStatusHint());
        assertTrue(ex.getMessage().contains("fullName"), ex.getMessage());
    }

    @Test
    void aMissingFullNameIsA400LikeABlankOne() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validateNew(new Customer("CUS-1008", null,
                        "no.name@example.com", null, CustomerStatus.PROSPECT, null), CORRELATION_ID));

        assertEquals(400, ex.getStatusHint());
        assertTrue(ex.getMessage().contains("fullName"), ex.getMessage());
    }

    @Test
    void aNullCustomerIsA400RatherThanANullPointer() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validateNew(null, CORRELATION_ID));

        assertEquals(400, ex.getStatusHint());
        assertEquals("VALIDATION_FAILED", ex.getCode());
        assertEquals(CORRELATION_ID, ex.getCorrelationId());
    }

    @Test
    void aCustomerWithNoIdIsA400() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validateNew(new Customer(null, "Amina Khan",
                        "amina.khan@example.com", null, CustomerStatus.ACTIVE, null), CORRELATION_ID));

        assertEquals(400, ex.getStatusHint());
        assertTrue(ex.getMessage().contains("customerId"), ex.getMessage());
    }

    @Test
    void aFirstCustomerWithAFreeEmailIsAccepted() {
        assertDoesNotThrow(() -> validator.validateNew(amina(), CORRELATION_ID));
    }

    private static Customer amina() {
        return new Customer("CUS-1001", "Amina Khan", "amina.khan@example.com",
                "555-0101", CustomerStatus.ACTIVE, null);
    }
}
