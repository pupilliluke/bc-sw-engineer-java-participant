package com.northstar.crm.service;

import com.northstar.crm.entity.Customer;
import com.northstar.crm.entity.CustomerStatus;
import com.northstar.crm.repository.InMemoryCustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
    void activeToProspectIsRejected() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> validator.validateTransition(
                        CustomerStatus.ACTIVE, CustomerStatus.PROSPECT, CORRELATION_ID));

        assertTrue(ex.getMessage().contains("ACTIVE -> PROSPECT"), ex.getMessage());
        assertTrue(ex.getMessage().contains(CORRELATION_ID),
                "support cannot trace the report without the correlation id");
    }

    @Test
    void sameStatusIsRejectedRatherThanIgnored() {
        assertThrows(IllegalStateException.class, () -> validator.validateTransition(
                CustomerStatus.ACTIVE, CustomerStatus.ACTIVE, CORRELATION_ID));
    }

    @Test
    void closedIsTerminal() {
        assertThrows(IllegalStateException.class, () -> validator.validateTransition(
                CustomerStatus.CLOSED, CustomerStatus.ACTIVE, CORRELATION_ID));
    }

    @Test
    void duplicateIdIsRejected() {
        repo.save(amina());

        assertThrows(IllegalStateException.class, () -> validator.validateNew(
                new Customer("CUS-1001", "Someone Else", "someone@example.com",
                        null, CustomerStatus.PROSPECT, null)));
    }

    @Test
    void duplicateEmailIsRejectedIgnoringCase() {
        repo.save(amina());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> validator.validateNew(new Customer("CUS-1009", "Amina K",
                        "AMINA.KHAN@example.com", null, CustomerStatus.PROSPECT, null)));

        assertTrue(ex.getMessage().contains("duplicate email"), ex.getMessage());
    }

    @Test
    void blankCustomerIdIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> validator.validateNew(
                new Customer("   ", "Amina Khan", "amina.khan@example.com",
                        null, CustomerStatus.ACTIVE, null)));
    }

    private static Customer amina() {
        return new Customer("CUS-1001", "Amina Khan", "amina.khan@example.com",
                "555-0101", CustomerStatus.ACTIVE, null);
    }
}
