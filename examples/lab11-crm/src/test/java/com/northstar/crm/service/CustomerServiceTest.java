package com.northstar.crm.service;

import com.northstar.crm.entity.Customer;
import com.northstar.crm.entity.CustomerStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomerServiceTest {

    private CustomerService service;

    @BeforeEach
    void setUp() {
        service = new CustomerService();
    }

    private static Customer amina() {
        return new Customer("CUS-1001", "Amina Khan", "amina.khan@example.com",
                "555-0101", CustomerStatus.ACTIVE, LocalDateTime.now());
    }

    private static Customer ravi() {
        return new Customer("CUS-1002", "Ravi Singh", "ravi.singh@example.com",
                "555-0102", CustomerStatus.PROSPECT, LocalDateTime.now());
    }

    @Test
    void addCustomerStoresNewCustomer() {
        service.addCustomer(amina());

        assertEquals(1, service.listAll().size());
        assertEquals("CUS-1001", service.listAll().get(0).getCustomerId());
        assertEquals(CustomerStatus.ACTIVE,
                service.findByCustomerId("CUS-1001").orElseThrow().getStatus());
    }

    @Test
    void addCustomerRejectsDuplicateId() {
        service.addCustomer(amina());
        Customer duplicate = new Customer("CUS-1001", "Someone Else", "someone@example.com",
                "555-0000", CustomerStatus.PROSPECT, LocalDateTime.now());

        assertThrows(IllegalStateException.class, () -> service.addCustomer(duplicate));
        assertEquals(1, service.listAll().size(), "a rejected add must not reach the list");
    }

    @Test
    void updateStatusChangesExistingCustomer() {
        service.addCustomer(ravi());

        service.updateStatus("CUS-1002", CustomerStatus.ACTIVE);

        assertEquals(CustomerStatus.ACTIVE,
                service.findByCustomerId("CUS-1002").orElseThrow().getStatus());
    }

    @Test
    void updateStatusThrowsForUnknownCustomer() {
        assertThrows(IllegalArgumentException.class,
                () -> service.updateStatus("CUS-9999", CustomerStatus.ACTIVE));
    }

    @Test
    void findByStatusReturnsOnlyMatchingCustomers() {
        service.addCustomer(amina());
        service.addCustomer(ravi());

        List<Customer> prospects = service.findByStatus(CustomerStatus.PROSPECT);

        assertEquals(1, prospects.size());
        assertEquals("CUS-1002", prospects.get(0).getCustomerId(),
                "Ravi is the only PROSPECT, Amina is ACTIVE and must be filtered out");
    }
}
