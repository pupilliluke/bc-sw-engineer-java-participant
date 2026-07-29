package com.northstar.crm.service;

import com.northstar.crm.dto.CustomerRequest;
import com.northstar.crm.dto.CustomerResponse;
import com.northstar.crm.entity.Customer;
import com.northstar.crm.entity.CustomerStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * In-memory store for Northstar CRM customers, plain Java only, no Spring and
 * no JPA. Holds customers in a List keyed by nothing but the customerId each
 * Customer carries, so every lookup scans the list.
 *
 * Not thread-safe. The lab runs single-threaded; a shared store with real
 * concurrency arrives with the database labs.
 */
public class CustomerService {
    private final List<Customer> customers = new ArrayList<>();

    /**
     * Add a customer. Reject blank customerId (IllegalArgumentException)
     * and duplicate customerId (IllegalStateException).
     */
    public Customer addCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("customer must not be null");
        }
        String id = customer.getCustomerId();
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("customerId must not be blank");
        }
        if (findByCustomerId(id).isPresent()) {
            throw new IllegalStateException("Duplicate customerId: " + id);
        }
        customers.add(customer);
        return customer;
    }

    /**
     * Find a customer by customerId.
     */
    public Optional<Customer> findByCustomerId(String customerId) {
        if (customerId == null) return Optional.empty();
        return customers.stream()
                .filter(c -> customerId.equals(c.getCustomerId()))
                .findFirst();
    }

    /**
     * Find customers by status. Returns an unmodifiable list.
     */
    public List<Customer> findByStatus(CustomerStatus status) {
        if (status == null) return List.of();
        return customers.stream()
                .filter(c -> status == c.getStatus())
                .toList();
    }

    /**
     * Update a customer's status. Throws IllegalArgumentException if the
     * customerId is blank, the new status is null, or the customer is not found.
     */
    public Customer updateStatus(String customerId, CustomerStatus newStatus) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId must not be blank");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("newStatus must not be null");
        }
        Customer customer = findByCustomerId(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer does not exist: " + customerId));
        customer.setStatus(newStatus);
        return customer;
    }

    /**
     * Delete a customer by customerId. Throws IllegalArgumentException if the
     * customerId is blank or no customer with that id exists. Returns the
     * removed customer.
     */
    public Customer deleteCustomer(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId must not be blank");
        }
        Customer customer = findByCustomerId(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer does not exist: " + customerId));
        customers.remove(customer);
        return customer;
    }

    /**
     * List all customers as an unmodifiable copy.
     */
    public List<Customer> listAll() {
        return List.copyOf(customers);
    }

    /**
     * Lab 8/9 DTO entry point, still a stub. CustomerController calls this, so
     * it stays until the DTO labs give it a body. Lab 10 works with the entity
     * methods above instead.
     */
    public CustomerResponse create(CustomerRequest request) {
        throw new UnsupportedOperationException("Lab 8 stub — implement later");
    }

    /**
     * Lab 8/9 DTO entry point, still a stub. See create(CustomerRequest).
     */
    public CustomerResponse getById(String customerId) {
        throw new UnsupportedOperationException("Lab 8 stub — implement later");
    }
}
