package com.northstar.crm.api;

import com.northstar.crm.model.Customer;
import com.northstar.crm.service.CustomerService;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    static final String CORRELATION_HEADER = "X-Correlation-Id";
    static final String DEFAULT_CORRELATION = "lab-request-001";

    private final CustomerService customers;

    public CustomerController(CustomerService customers) {
        this.customers = customers;
    }

    @PostMapping
    public ResponseEntity<Customer> create(
            @RequestHeader(value = CORRELATION_HEADER, required = false) String correlationId,
            @RequestBody Customer body) {
        String id = (correlationId == null || correlationId.isBlank())
                ? DEFAULT_CORRELATION
                : correlationId;
        Customer created = customers.create(body, id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(CORRELATION_HEADER, id)
                .body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> get(@PathVariable String id) {
        return customers.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
