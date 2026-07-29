import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Java entity class Customer in package com.northstar.crm.entity representing a
// Northstar CRM customer. Fields: customerId (String, format "CUS-1001"),
// fullName (String), email (String), phone (String), status (CustomerStatus
// enum: PROSPECT, ACTIVE, SUSPENDED, CLOSED), createdAt (LocalDateTime).
// No-args constructor, all-args constructor, getters and setters,
// equals/hashCode based only on customerId, toString.

enum CustomerStatus {
    PROSPECT,
    ACTIVE,
    SUSPENDED,
    CLOSED
}

class Customer {

    private String customerId;
    private String fullName;
    private String email;
    private String phone;
    private CustomerStatus status;
    private LocalDateTime createdAt;

    public Customer() {
    }

    public Customer(String customerId, String fullName, String email, String phone,
                    CustomerStatus status, LocalDateTime createdAt) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    public void setStatus(CustomerStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Customer other = (Customer) o;
        return Objects.equals(customerId, other.customerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId);
    }

    @Override
    public String toString() {
        return "Customer{customerId='" + customerId + "', fullName='" + fullName
                + "', status=" + status + "}";
    }
}

// Method addCustomer(Customer customer) on CustomerService: reject if customerId
// is null/blank, reject if a customer with the same customerId already exists
// (throw IllegalStateException), otherwise store it in the in-memory list and
// return it.

class CustomerService {

    private final List<Customer> customers = new ArrayList<>();

    public Customer addCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("customer must not be null");
        }
        if (customer.getCustomerId() == null || customer.getCustomerId().isBlank()) {
            throw new IllegalArgumentException("customerId must not be null or blank");
        }
        for (Customer existing : customers) {
            if (existing.getCustomerId().equals(customer.getCustomerId())) {
                throw new IllegalStateException(
                        "customer already exists: " + customer.getCustomerId());
            }
        }
        customers.add(customer);
        return customer;
    }

    public List<Customer> getCustomers() {
        return customers;
    }
}
