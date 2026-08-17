package com.northstar.crm.service;

import com.northstar.crm.api.FieldValidationException;
import com.northstar.crm.model.Customer;
import com.northstar.crm.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CustomerService {
  private static final Map<String, Set<String>> ALLOWED = Map.of(
      "PROSPECT", Set.of("ACTIVE", "CLOSED"),
      "ACTIVE", Set.of("SUSPENDED", "CLOSED"),
      "SUSPENDED", Set.of("ACTIVE", "CLOSED"),
      "CLOSED", Set.of());

  private final CustomerRepository customerRepository;

  public CustomerService(CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }

  public Customer create(Customer customer, String correlationId) {
    validate(customer);
    if (customerRepository.existsById(customer.getId())) {
      throw new IllegalStateException("Duplicate customer");
    }
    return customerRepository.save(customer);
  }

  public Customer update(String id, Customer incoming, String correlationId) {
    validate(incoming);
    Customer customer = get(id);
    customer.setName(incoming.getName());
    customer.setEmail(incoming.getEmail());
    customer.setStatus(incoming.getStatus());
    return customerRepository.save(customer);
  }

  private void validate(Customer customer) {
    Map<String, String> fieldErrors = new HashMap<>();
    if (customer.getName() == null || customer.getName().isBlank()) {
      fieldErrors.put("name", "Name is required");
    }
    if (customer.getEmail() == null || !customer.getEmail().matches("^\\S+@\\S+\\.\\S+$")) {
      fieldErrors.put("email", "Enter a valid email");
    }
    if (customer.getStatus() == null || !ALLOWED.containsKey(customer.getStatus())) {
      fieldErrors.put("status", "Choose a status");
    }
    if (!fieldErrors.isEmpty()) {
      throw new FieldValidationException(fieldErrors);
    }
  }

  public Customer get(String id) {
    return customerRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id));
  }

  public Customer updateStatus(String id, String newStatus, String correlationId) {
    if (newStatus == null || newStatus.isBlank()) {
      throw new FieldValidationException(Map.of("status", "newStatus is required"));
    }
    Customer customer = get(id);
    String from = customer.getStatus();
    if (!ALLOWED.getOrDefault(from, Set.of()).contains(newStatus)) {
      throw new IllegalStateException(
          "illegal status transition " + from + " -> " + newStatus + " [" + correlationId + "]");
    }
    customer.setStatus(newStatus);
    return customerRepository.save(customer);
  }

  public List<Customer> list() {
    return customerRepository.findAll();
  }
}
