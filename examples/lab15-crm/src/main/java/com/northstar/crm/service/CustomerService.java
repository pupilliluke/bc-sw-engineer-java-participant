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

    Customer addCustomer(Customer customer);

    Optional<Customer> findById(String customerId);

    List<Customer> listAll();

    /**
     * The correlation id is a parameter rather than constructor state, one
     * service instance serves many requests.
     */
    Customer changeStatus(String customerId, CustomerStatus newStatus, String correlationId);
}
