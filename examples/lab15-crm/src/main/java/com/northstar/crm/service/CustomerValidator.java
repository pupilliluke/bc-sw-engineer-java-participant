package com.northstar.crm.service;

import com.northstar.crm.entity.Customer;
import com.northstar.crm.entity.CustomerStatus;
import com.northstar.crm.repository.CustomerRepository;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Business rules, in one place. Lab 14's annotations check the shape of a
 * payload; these rules need the current store or the current status of a
 * customer, which an annotation cannot reach.
 *
 * Holds the repository for the uniqueness checks. It never writes.
 */
public class CustomerValidator {

    /**
     * The transition table. Anything not listed is illegal, so ACTIVE to ACTIVE
     * is rejected and CLOSED is terminal.
     */
    private static final Map<CustomerStatus, Set<CustomerStatus>> ALLOWED =
            new EnumMap<>(CustomerStatus.class);

    static {
        ALLOWED.put(CustomerStatus.PROSPECT, EnumSet.of(CustomerStatus.ACTIVE, CustomerStatus.CLOSED));
        ALLOWED.put(CustomerStatus.ACTIVE, EnumSet.of(CustomerStatus.SUSPENDED, CustomerStatus.CLOSED));
        ALLOWED.put(CustomerStatus.SUSPENDED, EnumSet.of(CustomerStatus.ACTIVE, CustomerStatus.CLOSED));
        ALLOWED.put(CustomerStatus.CLOSED, EnumSet.noneOf(CustomerStatus.class));
    }

    private final CustomerRepository repository;

    public CustomerValidator(CustomerRepository repository) {
        this.repository = repository;
    }

    /**
     * IllegalArgumentException for a customer that could never be stored,
     * IllegalStateException for one that conflicts with the current store.
     */
    public void validateNew(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("customer must be provided");
        }
        if (customer.getCustomerId() == null || customer.getCustomerId().isBlank()) {
            throw new IllegalArgumentException("customerId is required");
        }
        if (customer.getFullName() == null || customer.getFullName().isBlank()) {
            throw new IllegalArgumentException("fullName is required");
        }
        if (repository.existsById(customer.getCustomerId())) {
            throw new IllegalStateException("duplicate customerId: " + customer.getCustomerId());
        }
        if (repository.existsByEmail(customer.getEmail())) {
            throw new IllegalStateException("duplicate email: " + customer.getEmail());
        }
    }

    public void validateTransition(CustomerStatus from, CustomerStatus to, String correlationId) {
        if (to == null) {
            throw new IllegalArgumentException(
                    "newStatus is required [" + correlationId + "]");
        }
        Set<CustomerStatus> allowed = ALLOWED.getOrDefault(from, Set.of());
        if (!allowed.contains(to)) {
            throw new IllegalStateException(
                    "illegal status transition " + from + " -> " + to
                            + " [" + correlationId + "]");
        }
    }
}
