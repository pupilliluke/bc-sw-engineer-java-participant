package com.northstar.crm.service;

import com.northstar.crm.dto.CustomerRequest;
import com.northstar.crm.dto.CustomerResponse;
import com.northstar.crm.entity.Customer;
import com.northstar.crm.entity.CustomerStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * In-memory store for Northstar CRM customers, plain Java only, no Spring and
 * no JPA. Lab 11 refactor: the blank-customerId rule lives in exactly one
 * place, validateCustomerId, and a status change is announced through a
 * CustomerNotifier instead of a println, so a test can verify it.
 *
 * Not thread-safe. The lab runs single-threaded; a shared store with real
 * concurrency arrives with the database labs.
 */
public class CustomerService {

    private final List<Customer> customers = new ArrayList<>();
    private final CustomerNotifier notifier;

    /**
     * Lab 10 callers and Main construct the service with no arguments and no
     * interest in notifications, so the default is a notifier that does
     * nothing. Kept so the extract does not break existing code.
     */
    public CustomerService() {
        this((customerId, oldStatus, newStatus) -> { });
    }

    public CustomerService(CustomerNotifier notifier) {
        this.notifier = notifier;
    }

    /**
     * Add a customer. Rejects a null customer and a blank customerId
     * (IllegalArgumentException) and a duplicate customerId
     * (IllegalStateException). Missing createdAt and status are defaulted.
     */
    public Customer addCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("customer must not be null");
        }
        validateCustomerId(customer.getCustomerId());
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
     * Update a customer's status and announce the transition to the notifier.
     * Throws IllegalArgumentException if the customerId is blank, the new
     * status is null, or the customer is not found.
     */
    public Customer updateStatus(String customerId, CustomerStatus newStatus) {
        validateCustomerId(customerId);
        if (newStatus == null) {
            throw new IllegalArgumentException("newStatus must not be null");
        }
        Customer customer = findByCustomerId(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer does not exist: " + customerId));
        CustomerStatus oldStatus = customer.getStatus();
        customer.setStatus(newStatus);
        notifier.notifyStatusChange(customerId, oldStatus, newStatus);
        return customer;
    }

    /**
     * Delete a customer by customerId. Throws IllegalArgumentException if the
     * customerId is blank or no customer with that id exists. Returns the
     * removed customer. Carried over from Lab 10; the blank-id check that used
     * to be inline here now goes through validateCustomerId like the rest.
     */
    public Customer deleteCustomer(String customerId) {
        validateCustomerId(customerId);
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
     * it stays until the DTO labs give it a body. Lab 11 works with the entity
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

    /**
     * The single blank-customerId check. addCustomer, updateStatus and
     * deleteCustomer all call this; before Lab 11 each carried its own copy of
     * the rule.
     */
    private void validateCustomerId(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId must not be blank");
        }
    }
}
