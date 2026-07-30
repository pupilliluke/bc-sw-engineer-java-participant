package com.northstar.crm.api;

import com.northstar.crm.dto.CustomerRequestDTO;
import com.northstar.crm.dto.CustomerResponseDTO;
import com.northstar.crm.entity.Customer;
import com.northstar.crm.service.CustomerService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class CustomerApiFacadeTest {

    private static final String CORRELATION_ID = "lab-request-001";

    @Test
    void createReturnsResponseDtoForAmina() {
        CustomerApiFacade api = new CustomerApiFacade(new CustomerService());

        CustomerResponseDTO response = api.create(amina(), CORRELATION_ID);

        assertEquals("CUS-1001", response.getCustomerId());
        assertEquals("Amina Khan", response.getFullName());
        assertEquals("ACTIVE", response.getStatus());
        assertNotNull(response.getCreatedAt(), "createdAt is stamped by the service");
    }

    @Test
    void invalidEmailIsRejectedWithCorrelationIdAndStableCode() {
        CustomerApiFacade api = new CustomerApiFacade(new CustomerService());
        CustomerRequestDTO request = amina();
        request.setEmail("not-an-email");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> api.create(request, CORRELATION_ID));

        assertTrue(ex.getMessage().contains(CORRELATION_ID), ex.getMessage());
        assertTrue(ex.getMessage().contains(CustomerApiFacade.CODE_VALIDATION_FAILED), ex.getMessage());
        assertTrue(ex.getMessage().contains("email must be a valid address"), ex.getMessage());
    }

    @Test
    void invalidPayloadNeverReachesTheService() {
        CustomerService service = mock(CustomerService.class);
        CustomerApiFacade api = new CustomerApiFacade(service);
        CustomerRequestDTO request = amina();
        request.setFullName(" ");

        assertThrows(IllegalArgumentException.class, () -> api.create(request, CORRELATION_ID));

        verify(service, never()).createCustomer(any(Customer.class));
    }

    @Test
    void statusTypoFailsAtMappingNotValidation() {
        CustomerApiFacade api = new CustomerApiFacade(new CustomerService());
        CustomerRequestDTO request = amina();
        request.setStatus("ACTVE");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> api.create(request, CORRELATION_ID));

        assertTrue(ex.getMessage().contains(CustomerApiFacade.CODE_STATUS_INVALID), ex.getMessage());
        assertTrue(ex.getMessage().contains(CORRELATION_ID), ex.getMessage());
    }

    @Test
    void unknownIdIsNotFoundWithCorrelationId() {
        CustomerApiFacade api = new CustomerApiFacade(new CustomerService());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> api.get("CUS-9999", CORRELATION_ID));

        assertTrue(ex.getMessage().contains(CustomerApiFacade.CODE_NOT_FOUND), ex.getMessage());
        assertTrue(ex.getMessage().contains(CORRELATION_ID), ex.getMessage());
    }

    private CustomerRequestDTO amina() {
        return new CustomerRequestDTO("CUS-1001", "Amina Khan",
                "amina.khan@example.com", "555-0101", "ACTIVE");
    }
}
