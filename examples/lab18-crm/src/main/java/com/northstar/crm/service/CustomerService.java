package com.northstar.crm.service;

import com.northstar.crm.entity.Customer;
import com.northstar.crm.entity.CustomerStatus;
import java.util.List;
import java.util.Optional;

/**
 * The use-case API. Lab 14's concrete CustomerService became
 * DefaultCustomerService and the interface took the name, so the facade still
 * compiles against CustomerService.
 *
 * Four methods, no annotations, no persistence or Spring types.
 */
public interface CustomerService {

    /**
     * The correlation id joined this signature in Lab 16. Every rejection below
     * here is a BusinessException carrying the id, and addCustomer had no way
     * to supply one.
     */
    Customer addCustomer(Customer customer, String correlationId);

    Optional<Customer> findById(String customerId);

    List<Customer> listAll();

    Customer changeStatus(String customerId, CustomerStatus newStatus, String correlationId);
}
