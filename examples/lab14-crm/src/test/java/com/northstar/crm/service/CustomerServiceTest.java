package com.northstar.crm.service;

import com.northstar.crm.entity.Customer;
import com.northstar.crm.entity.CustomerStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerServiceTest {

    private CustomerService svc;

    @BeforeEach
    void setUp() {
        svc = new CustomerService();
    }

    @Test
    void createAminaKhanThenGetById() {
        Customer created = svc.createCustomer(
                "CUS-1001", "Amina Khan", "amina.khan@example.com", "555-0101", CustomerStatus.ACTIVE);

        assertEquals("CUS-1001", created.getCustomerId());
        assertEquals(CustomerStatus.ACTIVE, created.getStatus());
        assertEquals("Amina Khan", svc.getCustomer("CUS-1001").getFullName());
    }

    @Test
    void duplicateIdRejected() {
        svc.createCustomer("CUS-1002", "Ravi Singh", "ravi.singh@example.com",
                "555-0102", CustomerStatus.PROSPECT);

        assertThrows(IllegalStateException.class, () -> svc.createCustomer(
                "CUS-1002", "Other", "x@example.com", null, CustomerStatus.PROSPECT));
    }

    @Test
    void unknownCustomerFailsClearly() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> svc.getCustomer("CUS-9999"));

        assertTrue(thrown.getMessage().contains("CUS-9999"),
                "the message must name the id support is asking about");
        assertTrue(thrown.getMessage().contains("lab-request-001"),
                "and carry the correlation id, otherwise support cannot trace the request");
    }

    @Test
    void updateStatusMovesRaviFromProspectToActive() {
        svc.createCustomer("CUS-1002", "Ravi Singh", "ravi.singh@example.com",
                "555-0102", CustomerStatus.PROSPECT);

        Customer updated = svc.updateStatus("CUS-1002", CustomerStatus.ACTIVE);

        assertEquals(CustomerStatus.ACTIVE, updated.getStatus());
        assertEquals(CustomerStatus.ACTIVE, svc.getCustomer("CUS-1002").getStatus());
    }

    @Test
    void blankCustomerIdRejected() {
        assertThrows(IllegalArgumentException.class, () -> svc.createCustomer(
                "   ", "Amina Khan", "amina.khan@example.com", "555-0101", CustomerStatus.ACTIVE));
    }

    @Test
    void lookupByEqualValueNotSameReference() {
        svc.createCustomer("CUS-1001", "Amina Khan", "amina.khan@example.com",
                "555-0101", CustomerStatus.ACTIVE);

        String idFromElsewhere = new String("CUS-1001");

        assertEquals("Amina Khan", svc.getCustomer(idFromElsewhere).getFullName(),
                "the frozen get() compared with ==, so a non-interned id lost Amina entirely");
    }
}
