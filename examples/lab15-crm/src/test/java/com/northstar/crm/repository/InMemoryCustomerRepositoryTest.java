package com.northstar.crm.repository;

import com.northstar.crm.entity.Customer;
import com.northstar.crm.entity.CustomerStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryCustomerRepositoryTest {

    private CustomerRepository repo;

    @BeforeEach
    void setUp() {
        repo = new InMemoryCustomerRepository();
    }

    @Test
    void saveThenFindByIdReturnsTheCustomer() {
        repo.save(amina());

        assertEquals("Amina Khan", repo.findById("CUS-1001").orElseThrow().getFullName());
    }

    @Test
    void missingIdIsAnEmptyOptional() {
        assertTrue(repo.findById("CUS-9999").isEmpty());
        assertFalse(repo.existsById("CUS-9999"));
    }

    @Test
    void existsByEmailIgnoresCaseAndSurroundingSpace() {
        repo.save(amina());

        assertTrue(repo.existsByEmail("  AMINA.KHAN@example.com "));
        assertFalse(repo.existsByEmail("ravi.singh@example.com"));
        assertFalse(repo.existsByEmail(null));
    }

    @Test
    void findAllHandsBackACopy() {
        repo.save(amina());

        List<Customer> first = repo.findAll();
        first.clear();

        assertEquals(1, repo.findAll().size(), "clearing the result must not empty the store");
    }

    @Test
    void saveOnTheSameIdReplacesRatherThanDuplicates() {
        repo.save(amina());
        repo.save(amina());

        assertEquals(1, repo.findAll().size());
    }

    private static Customer amina() {
        return new Customer("CUS-1001", "Amina Khan", "amina.khan@example.com",
                "555-0101", CustomerStatus.ACTIVE, null);
    }
}
