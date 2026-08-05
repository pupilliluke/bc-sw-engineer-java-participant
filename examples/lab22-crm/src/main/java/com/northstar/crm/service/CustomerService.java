package com.northstar.crm.service;

import com.northstar.crm.exception.DuplicateCustomerException;
import com.northstar.crm.model.Customer;
import com.northstar.crm.repository.CustomerRepository;
import java.util.Optional;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.northstar.crm.metrics.CustomerMetrics;


@Service
public class CustomerService {
    private final CustomerRepository repository;
    private final CustomerMetrics metrics;
    private final NotificationService notifications;

    public CustomerService(CustomerRepository repository, CustomerMetrics metrics, NotificationService notifications) {
        this.repository = repository;
        this.metrics = metrics;
        this.notifications = notifications;
    }


    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    @PostConstruct
    void init() {
        log.info("CustomerService ready");
    }

    @PreDestroy
    void shutdown() {
        log.info("CustomerService shutting down");
    }

    public Customer create(Customer input, String correlationId) {
        MDC.put("customerId", input.getId());
        MDC.put("op", "customer.create");
        long startedAt = System.nanoTime();
        try {
            if (repository.findById(input.getId()).isPresent()) {
                throw new DuplicateCustomerException("Duplicate customerId: " + input.getId());
            }
            log.info("Creating customer");
            Customer saved = repository.save(input);
            notifications.notifyCreated(saved.getId(), correlationId);
            log.info("Customer created status={} durationMs={}", saved.getStatus(),
                    (System.nanoTime() - startedAt) / 1_000_000);
            metrics.recordCreate("success");
            return saved;
        } catch (DuplicateCustomerException e) {
            log.warn("Create rejected reason=duplicate");
            metrics.recordCreate("failure");
            throw e;
        } catch (Exception e) {
            log.error("Create failed", e);
            metrics.recordCreate("failure");
            throw e;
        } finally {
            MDC.remove("customerId");
            MDC.remove("op");
        }
    }

    public Customer get(String id) {
        return findById(id).orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id));
    }

    public Optional<Customer> findById(String id) {
        MDC.put("customerId", id);
        MDC.put("op", "customer.get");
        long startedAt = System.nanoTime();
        try {
            log.info("Loading customer");
            Optional<Customer> found = repository.findById(id);
            metrics.recordGet(found.isPresent() ? "success" : "not_found");
            metrics.recordGetLatency(System.nanoTime() - startedAt);
            return found;
        } catch (Exception e) {
            log.error("Get failed", e);
            metrics.recordGet("failure");
            throw e;
        } finally {
            MDC.remove("customerId");
            MDC.remove("op");
        }
    }

}
