package com.northstar.crm.mapper;

import com.northstar.crm.dto.CustomerRequestDTO;
import com.northstar.crm.dto.CustomerResponseDTO;
import com.northstar.crm.entity.Customer;
import com.northstar.crm.entity.CustomerStatus;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CustomerMapperTest {

    @Test
    void responseCarriesTheContractFieldsAndNothingElse() {
        Set<String> fields = Arrays.stream(CustomerResponseDTO.class.getDeclaredFields())
                .filter(f -> !f.isSynthetic())
                .map(Field::getName)
                .collect(Collectors.toSet());

        assertEquals(
                Set.of("customerId", "fullName", "email", "status", "createdAt", "updatedAt"),
                fields,
                "response contract changed — update the README table and the consumers");
    }

    @Test
    void phoneIsAcceptedOnTheRequestButNotReturned() {
        CustomerRequestDTO request = new CustomerRequestDTO(
                "CUS-1001", "Amina Khan", "amina.khan@example.com", "555-0101", "ACTIVE");

        Customer entity = CustomerMapper.toEntity(request);
        assertEquals("555-0101", entity.getPhone());

        CustomerResponseDTO response = CustomerMapper.toResponse(entity);
        assertEquals(-1, response.toString().indexOf("555-0101"),
                "phone reached the response");
    }

    @Test
    void toEntityDoesNotSetServerControlledTimestamps() {
        Customer entity = CustomerMapper.toEntity(new CustomerRequestDTO(
                "CUS-1002", "Ravi Singh", "ravi.singh@example.com", null, "PROSPECT"));

        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
        assertEquals(CustomerStatus.PROSPECT, entity.getStatus());
    }

    @Test
    void localDateTimeIsPublishedAsUtcInstant() {
        LocalDateTime stamped = LocalDateTime.of(2026, 7, 30, 12, 0);
        Customer entity = new Customer();
        entity.setCustomerId("CUS-1001");
        entity.setStatus(CustomerStatus.ACTIVE);
        entity.setCreatedAt(stamped);

        CustomerResponseDTO response = CustomerMapper.toResponse(entity);

        assertEquals(Instant.parse("2026-07-30T12:00:00Z"), response.getCreatedAt());
        assertEquals(stamped.toInstant(ZoneOffset.UTC), response.getCreatedAt());
        assertNull(response.getUpdatedAt());
    }
}
