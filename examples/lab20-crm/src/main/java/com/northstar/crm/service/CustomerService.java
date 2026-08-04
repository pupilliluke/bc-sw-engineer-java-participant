package com.northstar.crm.service;

import com.northstar.crm.exception.DuplicateCustomerException;
import com.northstar.crm.model.Customer;
import com.northstar.crm.repository.CustomerRepository;
import java.util.Optional;

import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CustomerService {
    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }


    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    public Customer create(Customer input) {
        MDC.put("customerId", input.getCustomerId());
        MDC.put("op", "customer.create");
        long startedAt = System.nanoTime();
        try {
            if (repository.findById(input.getCustomerId()).isPresent()) {
                throw new DuplicateCustomerException("Duplicate customerId: " + input.getCustomerId());
            }
            log.info("Creating customer");
            Customer saved = repository.save(input);
            log.info("Customer created status={} durationMs={}", saved.getStatus(),
                    (System.nanoTime() - startedAt) / 1_000_000);
            return saved;
        } catch (DuplicateCustomerException e) {
            log.warn("Create rejected reason=duplicate");
            throw e;
        } catch (Exception e) {
            log.error("Create failed", e);
            throw e;
        } finally {
            MDC.remove("customerId");
            MDC.remove("op");
        }
    }

    public Optional<Customer> findById(String id) {
        MDC.put("customerId", id);
        MDC.put("op", "customer.get");
        try {
            log.info("Loading customer");
            return repository.findById(id);
        } finally {
            MDC.remove("customerId");
            MDC.remove("op");
        }
    }

}
