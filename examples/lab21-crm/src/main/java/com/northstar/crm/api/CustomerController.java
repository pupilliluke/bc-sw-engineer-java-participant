package com.northstar.crm.api;

import com.northstar.crm.exception.DuplicateCustomerException;
import com.northstar.crm.model.Customer;
import com.northstar.crm.service.CustomerService;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.northstar.crm.metrics.CustomerMetrics;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customers;
    private final CustomerMetrics metrics;

    public CustomerController(CustomerService customers, CustomerMetrics metrics) {
        this.customers = customers;
        this.metrics = metrics;
    }

    @PostMapping
    public ResponseEntity<Customer> create(@RequestBody Customer body) {
        if (body.getCustomerId() == null || body.getCustomerId().isBlank()) {
            log.warn("Rejecting create reason=invalid_id");
            throw new IllegalArgumentException("customerId required");
        }
        if (body.getFullName() == null || body.getFullName().isBlank()) {
            log.warn("Rejecting create reason=missing_full_name customerId={}", body.getCustomerId());
            throw new IllegalArgumentException("fullName required");
        }
        if (body.getStatus() == null || body.getStatus().isBlank()) {
            log.warn("Rejecting create reason=missing_status customerId={}", body.getCustomerId());
            throw new IllegalArgumentException("status required");
        }
        Customer created = customers.create(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> get(@PathVariable String id) {
        return customers.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException ex) {
        metrics.recordCreate("failure");
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateCustomerException.class)
    ResponseEntity<Map<String, String>> conflict(DuplicateCustomerException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }
}
