package com.northstar.crm.service;

import com.northstar.crm.entity.Customer;
import com.northstar.crm.entity.CustomerStatus;
import com.northstar.crm.exception.BusinessException;
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
 * Every rejection is a BusinessException now. Lab 15 threw IllegalArgument for
 * malformed and IllegalState for conflicting, and the facade had to infer a
 * client status from the JDK type it caught.
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
     * 400 for a customer that could never be stored, 409 for one that conflicts
     * with what is stored now.
     */
    public void validateNew(Customer customer, String correlationId) {
        if (customer == null) {
            throw BusinessException.validation("customer", "must be provided", correlationId);
        }
        if (customer.getCustomerId() == null || customer.getCustomerId().isBlank()) {
            throw BusinessException.validation("customerId", "is required", correlationId);
        }
        if (customer.getFullName() == null || customer.getFullName().isBlank()) {
            throw BusinessException.validation("fullName", "is required", correlationId);
        }
        if (repository.existsById(customer.getCustomerId())) {
            throw BusinessException.conflict(
                    "duplicate customerId: " + customer.getCustomerId(), correlationId);
        }
        if (repository.existsByEmail(customer.getEmail())) {
            // The address itself stays out of the message. The id is a stable
            // internal identifier; the email is the customer's own data.
            throw BusinessException.conflict(
                    "email is already registered for another customer", correlationId);
        }
    }

    public void validateTransition(CustomerStatus from, CustomerStatus to, String correlationId) {
        if (to == null) {
            throw BusinessException.validation("newStatus", "is required", correlationId);
        }
        Set<CustomerStatus> allowed = ALLOWED.getOrDefault(from, Set.of());
        if (!allowed.contains(to)) {
            throw BusinessException.conflict(
                    "illegal status transition " + from + " -> " + to, correlationId);
        }
    }
}
