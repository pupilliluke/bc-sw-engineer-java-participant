package com.northstar.crm.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerRequestDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void acceptsAminaKhan() {
        CustomerRequestDTO dto = validTemplate();
        dto.setCustomerId("CUS-1001");
        dto.setFullName("Amina Khan");
        dto.setEmail("amina.khan@example.com");
        dto.setStatus("ACTIVE");
        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    void rejectsInvalidEmail() {
        CustomerRequestDTO dto = validTemplate();
        dto.setEmail("not-an-email");
        assertEquals("email must be a valid address", onlyMessage(dto));
    }

    @Test
    void rejectsBlankFullName() {
        CustomerRequestDTO dto = validTemplate();
        dto.setFullName(" ");
        assertFalse(validator.validate(dto).isEmpty());
        assertTrue(messages(dto).contains("fullName is required"));
    }

    @Test
    void rejectsNullCustomerId() {
        CustomerRequestDTO dto = validTemplate();
        dto.setCustomerId(null);
        assertEquals("customerId is required", onlyMessage(dto));
    }

    @Test
    void rejectsFullNameOverMaxLengthButAcceptsTheBoundary() {
        CustomerRequestDTO atLimit = validTemplate();
        atLimit.setFullName("A".repeat(100));
        assertTrue(validator.validate(atLimit).isEmpty());

        CustomerRequestDTO overLimit = validTemplate();
        overLimit.setFullName("A".repeat(101));
        assertEquals("fullName must be between 2 and 100 characters", onlyMessage(overLimit));
    }

    private String onlyMessage(CustomerRequestDTO dto) {
        Set<ConstraintViolation<CustomerRequestDTO>> violations = validator.validate(dto);
        assertEquals(1, violations.size(), () -> "expected one violation, got " + messages(dto));
        return violations.iterator().next().getMessage();
    }

    private String messages(CustomerRequestDTO dto) {
        return validator.validate(dto).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
    }

    private CustomerRequestDTO validTemplate() {
        CustomerRequestDTO dto = new CustomerRequestDTO();
        dto.setCustomerId("CUS-1002");
        dto.setFullName("Ravi Singh");
        dto.setEmail("ravi.singh@example.com");
        dto.setPhone("555-0102");
        dto.setStatus("PROSPECT");
        return dto;
    }
}
