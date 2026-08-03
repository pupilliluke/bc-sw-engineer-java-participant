package com.northstar.crm.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerTest {

    @Test
    void equalsIsBasedOnCustomerIdOnly() {
        Customer amina = new Customer("CUS-1001", "Amina Khan", "amina.khan@example.com",
                "555-0101", CustomerStatus.ACTIVE, LocalDateTime.now());
        Customer sameIdEverythingElseDifferent = new Customer("CUS-1001", "Amina Khan-Patel",
                "amina.khanpatel@example.com", "555-0199", CustomerStatus.CLOSED,
                LocalDateTime.now().minusDays(30));

        assertEquals(amina, sameIdEverythingElseDifferent,
                "identity is customerId only, a rename must not create a second customer");
        Customer ravi = new Customer("CUS-1002", "Amina Khan", "amina.khan@example.com",
                "555-0101", CustomerStatus.ACTIVE, LocalDateTime.now());
        assertNotEquals(amina, ravi,
                "different customerId is a different customer even when every other field matches");
    }

    @Test
    void toStringIncludesCustomerId() {
        Customer ravi = new Customer("CUS-1002", "Ravi Singh", "ravi.singh@example.com",
                "555-0102", CustomerStatus.PROSPECT, LocalDateTime.now());

        assertTrue(ravi.toString().contains("CUS-1002"),
                "log lines identify a customer by id, toString must carry it");
    }
}
