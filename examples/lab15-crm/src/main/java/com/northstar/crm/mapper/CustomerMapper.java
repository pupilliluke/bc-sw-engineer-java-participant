package com.northstar.crm.mapper;

import com.northstar.crm.dto.CustomerRequestDTO;
import com.northstar.crm.dto.CustomerResponseDTO;
import com.northstar.crm.entity.Customer;
import com.northstar.crm.entity.CustomerStatus;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * The only class that imports both entity and DTO. The two directions do not
 * carry the same fields, so they are written out by hand.
 *
 * toEntity leaves the timestamps null; CustomerService stamps them. The entity
 * keeps LocalDateTime from Lab 10, the response publishes Instant, converted at
 * UTC in one place.
 */
public final class CustomerMapper {

    private CustomerMapper() {
    }

    public static Customer toEntity(CustomerRequestDTO request) {
        Customer customer = new Customer();
        customer.setCustomerId(request.getCustomerId());
        customer.setFullName(request.getFullName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setStatus(CustomerStatus.valueOf(request.getStatus()));
        return customer;
    }

    public static CustomerResponseDTO toResponse(Customer entity) {
        return CustomerResponseDTO.of(
                entity.getCustomerId(),
                entity.getFullName(),
                entity.getEmail(),
                entity.getStatus() == null ? null : entity.getStatus().name(),
                toInstant(entity.getCreatedAt()),
                toInstant(entity.getUpdatedAt()));
    }

    private static Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
