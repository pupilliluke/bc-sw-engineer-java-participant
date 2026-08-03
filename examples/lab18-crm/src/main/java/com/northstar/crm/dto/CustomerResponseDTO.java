package com.northstar.crm.dto;

import java.time.Instant;

/**
 * Outbound contract. Six fields, chosen rather than inherited — the entity also
 * holds phone and phone does not come back out. Factory plus getters, no
 * setters.
 */
public class CustomerResponseDTO {

    private String customerId;
    private String fullName;
    private String email;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    private CustomerResponseDTO() {
    }

    public static CustomerResponseDTO of(
            String customerId, String fullName, String email,
            String status, Instant createdAt, Instant updatedAt) {
        CustomerResponseDTO dto = new CustomerResponseDTO();
        dto.customerId = customerId;
        dto.fullName = fullName;
        dto.email = email;
        dto.status = status;
        dto.createdAt = createdAt;
        dto.updatedAt = updatedAt;
        return dto;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "CustomerResponseDTO{customerId='" + customerId + "', fullName='" + fullName
                + "', email='" + email + "', status=" + status
                + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "}";
    }
}
