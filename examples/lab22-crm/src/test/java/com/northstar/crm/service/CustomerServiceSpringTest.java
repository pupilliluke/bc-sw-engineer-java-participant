package com.northstar.crm.service;

import com.northstar.crm.exception.DuplicateCustomerException;
import com.northstar.crm.model.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CustomerServiceSpringTest {

    @Autowired
    private CustomerService service;

    @Test
    void springGraphCreatesAndGetsCus1001() {
        Customer seeded = service.get("CUS-1001");
        assertEquals("CUS-1001", seeded.getId());
        assertEquals("Amina Khan", seeded.getName());

        Customer created = service.create(new Customer("CUS-2202", "Priya Patel",
                "priya.patel@example.com", "PROSPECT"), "lab-request-001");
        assertEquals("CUS-2202", created.getId());
        assertEquals("Priya Patel", service.get("CUS-2202").getName());

        assertThrows(DuplicateCustomerException.class,
                () -> service.create(Customer.ravi(), "lab-request-001"));
        assertEquals("Ravi Singh", service.get("CUS-1002").getName());
    }
}
