package com.northstar.crm.service;

import com.northstar.crm.entity.Customer;
import com.northstar.crm.entity.CustomerStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Lab 10 baseline. Lab 11 TODOs: extract CustomerNotifier + validation helper;
 * keep behavior for CUS-1001 / CUS-1002.
 */
public class CustomerService {
    private final List<Customer> customers = new ArrayList<>();
    // TODO: inject/use CustomerNotifier instead of System.out after refactor

    public Customer addCustomer(Customer customer) {
        // TODO: extract duplicated validation into a private helper (Lab 11)
        if (customer == null || customer.getCustomerId() == null || customer.getCustomerId().isBlank()) {
            throw new IllegalArgumentException("customerId is required");
        }
        if (findByCustomerId(customer.getCustomerId()).isPresent()) {
            throw new IllegalStateException("Duplicate customerId: " + customer.getCustomerId());
        }
        if (customer.getCreatedAt() == null) {
            customer.setCreatedAt(LocalDateTime.now());
        }
        if (customer.getStatus() == null) {
            customer.setStatus(CustomerStatus.PROSPECT);
        }
        customers.add(customer);
        System.out.println("created " + customer.getCustomerId()); // TODO: notify via CustomerNotifier
        return customer;
    }

    public Optional<Customer> findByCustomerId(String customerId) {
        return customers.stream()
                .filter(c -> c.getCustomerId().equals(customerId))
                .findFirst();
    }

    public Customer updateStatus(String customerId, CustomerStatus status) {
        Customer c = findByCustomerId(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));
        c.setStatus(status);
        return c;
    }
}
