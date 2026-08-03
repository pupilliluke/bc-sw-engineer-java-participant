package com.northstar.crm.service;

import com.northstar.crm.entity.Customer;
import com.northstar.crm.entity.CustomerStatus;
import com.northstar.crm.exception.BusinessException;
import com.northstar.crm.repository.CustomerRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Lab 14's CustomerService class, renamed. It no longer holds the Map or the
 * rules, it calls the validator and then the repository, and it stamps the
 * timestamps rather than accepting them from the caller.
 *
 * Both collaborators arrive through the constructor.
 */
public class DefaultCustomerService implements CustomerService {

    private final CustomerRepository repository;
    private final CustomerValidator validator;

    public DefaultCustomerService(CustomerRepository repository, CustomerValidator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    @Override
    public Customer addCustomer(Customer customer, String correlationId) {
        validator.validateNew(customer, correlationId);
        if (customer.getStatus() == null) {
            customer.setStatus(CustomerStatus.PROSPECT);
        }
        LocalDateTime now = LocalDateTime.now();
        customer.setCreatedAt(now);
        customer.setUpdatedAt(now);
        return repository.save(customer);
    }

    @Override
    public Optional<Customer> findById(String customerId) {
        return repository.findById(customerId);
    }

    /** A copy, so a caller cannot add or remove customers by mutating the result. */
    @Override
    public List<Customer> listAll() {
        return List.copyOf(repository.findAll());
    }

    /**
     * Look up, validate, then mutate. A rejected transition leaves the stored
     * status unchanged.
     */
    @Override
    public Customer changeStatus(String customerId, CustomerStatus newStatus, String correlationId) {
        Customer existing = repository.findById(customerId)
                .orElseThrow(() -> BusinessException.notFound(customerId, correlationId));
        validator.validateTransition(existing.getStatus(), newStatus, correlationId);
        existing.setStatus(newStatus);
        existing.setUpdatedAt(LocalDateTime.now());
        return repository.save(existing);
    }
}
