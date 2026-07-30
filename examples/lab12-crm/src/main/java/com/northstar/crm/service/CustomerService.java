package com.northstar.crm.service;

import com.northstar.crm.dto.CustomerRequest;
import com.northstar.crm.dto.CustomerResponse;
import com.northstar.crm.entity.Customer;
import com.northstar.crm.entity.CustomerStatus;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * Cleaned up CustomerService API: intention-revealing methods and explicit
 * status updates. Backing store is a typed Map keyed by customerId, so lookups
 * compare id values rather than references.
 *
 * Every failure message carries the correlation id so support can tie a report
 * back to one request. Real MDC arrives with a logging framework; this lab
 * concatenates.
 */
public class CustomerService {

    private static final String DEFAULT_CORRELATION_ID = "lab-request-001";

    private final Map<String, Customer> customersById = new HashMap<>();
    private final String correlationId;

    public CustomerService() {
        this(DEFAULT_CORRELATION_ID);
    }

    public CustomerService(String correlationId) {
        this.correlationId = correlationId;
    }

    public Customer createCustomer(String customerId, String fullName, String email,
                                   String phone, CustomerStatus status) {
        requireNonBlank(customerId, "customerId");
        requireNonBlank(fullName, "fullName");
        requireUniqueId(customerId);

        Customer c = new Customer();
        c.setCustomerId(customerId);
        c.setFullName(fullName);
        c.setEmail(email);
        c.setPhone(phone);
        c.setStatus(status == null ? CustomerStatus.PROSPECT : status);
        c.setCreatedAt(LocalDateTime.now());

        customersById.put(customerId, c);
        return c;
    }

    public Customer getCustomer(String customerId) {
        return requireExisting(customerId);
    }

    public Customer updateStatus(String customerId, CustomerStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("newStatus must be provided" + correlationSuffix());
        }
        Customer existing = requireExisting(customerId);
        existing.setStatus(newStatus);
        return existing;
    }

    /* Helpers — each rule lives in exactly one of these. */
    private void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must be provided" + correlationSuffix());
        }
    }

    private void requireUniqueId(String customerId) {
        if (customersById.containsKey(customerId)) {
            throw new IllegalStateException(
                    "Customer id already exists: " + customerId + correlationSuffix());
        }
    }

    private Customer requireExisting(String customerId) {
        requireNonBlank(customerId, "customerId");
        Customer found = customersById.get(customerId);
        if (found == null) {
            throw new IllegalArgumentException(
                    "Customer not found: " + customerId + correlationSuffix());
        }
        return found;
    }

    private String correlationSuffix() {
        return " correlationId=" + correlationId;
    }

    // Legacy DTO stubs — preserved for controllers/tests that call them.
    // They delegate to the new typed API where possible.
    public CustomerResponse create(CustomerRequest request) {
        throw new UnsupportedOperationException("Lab 8 stub — implement later");
    }

    public CustomerResponse getById(String customerId) {
        throw new UnsupportedOperationException("Lab 8 stub — implement later");
    }
}
