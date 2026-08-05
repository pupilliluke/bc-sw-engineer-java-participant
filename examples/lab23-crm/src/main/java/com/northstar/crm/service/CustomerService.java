package com.northstar.crm.service;

import com.northstar.crm.model.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CustomerService {
  private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

  private final Map<String, Customer> store = new ConcurrentHashMap<>();

  public CustomerService() {
    store.put("CUS-1001", Customer.amina());
    store.put("CUS-1002", Customer.ravi());
  }

  public Customer create(Customer customer, String correlationId) {
    if (customer.getId() == null || customer.getId().isBlank()) {
      throw new IllegalArgumentException("Customer id required");
    }
    store.put(customer.getId(), customer);
    log.info("customer.created id={} status={} correlationId={}",
        customer.getId(), customer.getStatus(), correlationId);
    return customer;
  }

  public Customer get(String id) {
    Customer found = store.get(id);
    if (found == null) {
      throw new IllegalArgumentException("Customer not found: " + id);
    }
    return found;
  }
}
