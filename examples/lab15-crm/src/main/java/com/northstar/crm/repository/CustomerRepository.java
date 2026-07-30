package com.northstar.crm.repository;

import com.northstar.crm.entity.Customer;
import java.util.List;
import java.util.Optional;

/**
 * The persistence port, replacing the Lab 8 stub class. Callers depend on this
 * interface, not on a Map, a Connection or an EntityManager.
 *
 * Storage only. No method here decides whether a status transition is legal,
 * that rule is in CustomerValidator.
 */
public interface CustomerRepository {

    Customer save(Customer customer);

    Optional<Customer> findById(String customerId);

    boolean existsById(String customerId);

    boolean existsByEmail(String email);

    List<Customer> findAll();
}
