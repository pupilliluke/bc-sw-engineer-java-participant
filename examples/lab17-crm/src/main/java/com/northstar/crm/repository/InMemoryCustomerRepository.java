package com.northstar.crm.repository;

import com.northstar.crm.entity.Customer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * The only class that knows the store is a Map. The field is private with no
 * getter, and findAll returns a copy.
 *
 * LinkedHashMap rather than HashMap, so findAll and the Main transcript come
 * out in insertion order.
 *
 * Email comparison is case-insensitive, trimmed and lowercased with
 * Locale.ROOT, so AMINA.KHAN@example.com matches amina.khan@example.com.
 */
public class InMemoryCustomerRepository implements CustomerRepository {

    private final Map<String, Customer> customersById = new LinkedHashMap<>();

    @Override
    public Customer save(Customer customer) {
        customersById.put(customer.getCustomerId(), customer);
        return customer;
    }

    @Override
    public Optional<Customer> findById(String customerId) {
        return Optional.ofNullable(customersById.get(customerId));
    }

    @Override
    public boolean existsById(String customerId) {
        return customersById.containsKey(customerId);
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null) {
            return false;
        }
        String needle = normalise(email);
        return customersById.values().stream()
                .map(Customer::getEmail)
                .filter(stored -> stored != null)
                .anyMatch(stored -> normalise(stored).equals(needle));
    }

    @Override
    public List<Customer> findAll() {
        return new ArrayList<>(customersById.values());
    }

    private static String normalise(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
