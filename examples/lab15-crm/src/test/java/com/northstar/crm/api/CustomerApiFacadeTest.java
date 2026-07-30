package com.northstar.crm.api;

import com.northstar.crm.dto.CustomerRequestDTO;
import com.northstar.crm.dto.CustomerResponseDTO;
import com.northstar.crm.entity.Customer;
import com.northstar.crm.repository.CustomerRepository;
import com.northstar.crm.repository.InMemoryCustomerRepository;
import com.northstar.crm.service.CustomerService;
import com.northstar.crm.service.CustomerValidator;
import com.northstar.crm.service.DefaultCustomerService;
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
        CustomerApiFacade api = new CustomerApiFacade(realService());

        CustomerResponseDTO response = api.create(amina(), CORRELATION_ID);

        assertEquals("CUS-1001", response.getCustomerId());
        assertEquals("Amina Khan", response.getFullName());
        assertEquals("ACTIVE", response.getStatus());
        assertNotNull(response.getCreatedAt(), "createdAt is stamped by the service");
    }

    @Test
    void invalidEmailIsRejectedWithCorrelationIdAndStableCode() {
        CustomerApiFacade api = new CustomerApiFacade(realService());
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

        verify(service, never()).addCustomer(any(Customer.class));
    }

    @Test
    void statusTypoFailsAtMappingNotValidation() {
        CustomerApiFacade api = new CustomerApiFacade(realService());
        CustomerRequestDTO request = amina();
        request.setStatus("ACTVE");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> api.create(request, CORRELATION_ID));

        assertTrue(ex.getMessage().contains(CustomerApiFacade.CODE_STATUS_INVALID), ex.getMessage());
        assertTrue(ex.getMessage().contains(CORRELATION_ID), ex.getMessage());
    }

    @Test
    void unknownIdIsNotFoundWithCorrelationId() {
        CustomerApiFacade api = new CustomerApiFacade(realService());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> api.get("CUS-9999", CORRELATION_ID));

        assertTrue(ex.getMessage().contains(CustomerApiFacade.CODE_NOT_FOUND), ex.getMessage());
        assertTrue(ex.getMessage().contains(CORRELATION_ID), ex.getMessage());
    }

    @Test
    void duplicateCreateBecomesAConflictCode() {
        CustomerApiFacade api = new CustomerApiFacade(realService());
        api.create(amina(), CORRELATION_ID);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> api.create(amina(), CORRELATION_ID));

        assertTrue(ex.getMessage().contains(CustomerApiFacade.CODE_CONFLICT), ex.getMessage());
        assertTrue(ex.getMessage().contains(CORRELATION_ID), ex.getMessage());
    }

    @Test
    void activateThroughTheFacadeReturnsAnActiveResponseDto() {
        CustomerApiFacade api = new CustomerApiFacade(realService());
        api.create(ravi(), CORRELATION_ID);

        CustomerResponseDTO response = api.changeStatus("CUS-1002", "ACTIVE", CORRELATION_ID);

        assertEquals("ACTIVE", response.getStatus());
    }

    @Test
    void illegalTransitionCarriesTheTransitionCode() {
        CustomerApiFacade api = new CustomerApiFacade(realService());
        api.create(amina(), CORRELATION_ID);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> api.changeStatus("CUS-1001", "PROSPECT", CORRELATION_ID));

        assertTrue(ex.getMessage().contains(CustomerApiFacade.CODE_TRANSITION_INVALID), ex.getMessage());
        assertTrue(ex.getMessage().contains("ACTIVE -> PROSPECT"), ex.getMessage());
        assertEquals("ACTIVE", api.get("CUS-1001", CORRELATION_ID).getStatus());
    }

    private static CustomerService realService() {
        CustomerRepository repo = new InMemoryCustomerRepository();
        return new DefaultCustomerService(repo, new CustomerValidator(repo));
    }

    private CustomerRequestDTO amina() {
        return new CustomerRequestDTO("CUS-1001", "Amina Khan",
                "amina.khan@example.com", "555-0101", "ACTIVE");
    }

    private CustomerRequestDTO ravi() {
        return new CustomerRequestDTO("CUS-1002", "Ravi Singh",
                "ravi.singh@example.com", "555-0102", "PROSPECT");
    }
}
