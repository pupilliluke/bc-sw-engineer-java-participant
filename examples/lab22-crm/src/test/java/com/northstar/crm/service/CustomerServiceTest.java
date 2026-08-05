package com.northstar.crm.service;

import com.northstar.crm.metrics.CustomerMetrics;
import com.northstar.crm.model.Customer;
import com.northstar.crm.repository.InMemoryCustomerRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CustomerServiceTest {
    @Test
    void createAndGetWithoutSpringContext() {
        var repo = new InMemoryCustomerRepository();
        var metrics = new CustomerMetrics(new SimpleMeterRegistry());
        var notify = new NotificationService();
        var service = new CustomerService(repo, metrics, notify);

        Customer created = service.create(new Customer("CUS-2001", "Priya Patel",
                "priya.patel@example.com", "PROSPECT"), "lab-request-001");
        assertEquals("CUS-2001", created.getId());
        assertEquals("Priya Patel", service.get("CUS-2001").getName());
        assertEquals("Amina Khan", service.get("CUS-1001").getName());
    }
}
