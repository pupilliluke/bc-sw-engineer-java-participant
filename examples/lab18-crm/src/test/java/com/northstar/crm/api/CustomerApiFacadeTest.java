package com.northstar.crm.api;

import com.northstar.crm.dto.CustomerRequestDTO;
import com.northstar.crm.entity.Customer;
import com.northstar.crm.exception.ErrorResponse;
import com.northstar.crm.repository.CustomerRepository;
import com.northstar.crm.repository.InMemoryCustomerRepository;
import com.northstar.crm.service.CustomerService;
import com.northstar.crm.service.CustomerValidator;
import com.northstar.crm.service.DefaultCustomerService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerApiFacadeTest {

    private static final String CORRELATION_ID = "lab-request-001";

    @Test
    void createReturnsOkForAmina() {
        CustomerApiFacade api = new CustomerApiFacade(realService());

        ApiResult result = api.create(amina(), CORRELATION_ID);

        ApiResult.Ok ok = assertInstanceOf(ApiResult.Ok.class, result);
        assertEquals("CUS-1001", ok.body().getCustomerId());
        assertEquals("ACTIVE", ok.body().getStatus());
        assertNotNull(ok.body().getCreatedAt(), "createdAt is stamped by the service");
    }

    @Test
    void invalidEmailFailsWith400AndTheFieldName() {
        CustomerApiFacade api = new CustomerApiFacade(realService());
        CustomerRequestDTO request = amina();
        request.setEmail("not-an-email");

        ErrorResponse error = failure(api.create(request, CORRELATION_ID));

        assertEquals(400, error.getStatus());
        assertEquals("VALIDATION_FAILED", error.getError());
        assertEquals("email must be a valid address", error.getErrors().get("email"));
        assertEquals(CORRELATION_ID, error.getCorrelationId());
    }

    @Test
    void invalidPayloadNeverReachesTheService() {
        CustomerService service = mock(CustomerService.class);
        CustomerApiFacade api = new CustomerApiFacade(service);
        CustomerRequestDTO request = amina();
        request.setFullName(" ");

        assertEquals(400, failure(api.create(request, CORRELATION_ID)).getStatus());

        verify(service, never()).addCustomer(any(Customer.class), anyString());
    }

    @Test
    void statusTypoFailsAtMappingNotValidation() {
        CustomerApiFacade api = new CustomerApiFacade(realService());
        CustomerRequestDTO request = amina();
        request.setStatus("ACTVE");

        ErrorResponse error = failure(api.create(request, CORRELATION_ID));

        assertEquals(400, error.getStatus());
        assertTrue(error.getErrors().get("status").contains("ACTVE"), error.toJson());
    }

    @Test
    void unknownIdFailsWith404() {
        CustomerApiFacade api = new CustomerApiFacade(realService());

        ErrorResponse error = failure(api.getById("CUS-9999", CORRELATION_ID));

        assertEquals(404, error.getStatus());
        assertEquals("CUSTOMER_NOT_FOUND", error.getError());
        assertTrue(error.getMessage().contains("CUS-9999"), error.getMessage());
        assertEquals(CORRELATION_ID, error.getCorrelationId());
    }

    @Test
    void duplicateCreateFailsWith409() {
        CustomerApiFacade api = new CustomerApiFacade(realService());
        api.create(amina(), CORRELATION_ID);

        ErrorResponse error = failure(api.create(amina(), CORRELATION_ID));

        assertEquals(409, error.getStatus());
        assertEquals("BUSINESS_CONFLICT", error.getError());
    }

    @Test
    void activateThroughTheFacadeReturnsAnActiveResponse() {
        CustomerApiFacade api = new CustomerApiFacade(realService());
        api.create(ravi(), CORRELATION_ID);

        ApiResult.Ok ok = assertInstanceOf(ApiResult.Ok.class,
                api.changeStatus("CUS-1002", "ACTIVE", CORRELATION_ID));

        assertEquals("ACTIVE", ok.body().getStatus());
    }

    @Test
    void illegalTransitionFailsWith409AndLeavesAminaActive() {
        CustomerApiFacade api = new CustomerApiFacade(realService());
        api.create(amina(), CORRELATION_ID);

        ErrorResponse error = failure(api.changeStatus("CUS-1001", "PROSPECT", CORRELATION_ID));

        assertEquals(409, error.getStatus());
        assertTrue(error.getMessage().contains("ACTIVE -> PROSPECT"), error.getMessage());
        ApiResult.Ok after = assertInstanceOf(ApiResult.Ok.class,
                api.getById("CUS-1001", CORRELATION_ID));
        assertEquals("ACTIVE", after.body().getStatus());
    }

    /**
     * The catch order under test. A storage failure is not a BusinessException,
     * so it has to fall through to the generic 500 with nothing of the cause in
     * the payload.
     */
    @Test
    void unexpectedFailureBecomesAGeneric500() {
        CustomerService service = mock(CustomerService.class);
        when(service.findById("CUS-1001"))
                .thenThrow(new RuntimeException("connection to crm-db refused"));
        CustomerApiFacade api = new CustomerApiFacade(service);

        ErrorResponse error = failure(api.getById("CUS-1001", CORRELATION_ID));

        assertEquals(500, error.getStatus());
        assertEquals("Unexpected server error", error.getMessage());
        assertFalse(error.toJson().contains("crm-db"), error.toJson());
        assertEquals(CORRELATION_ID, error.getCorrelationId());
    }

    @Test
    void blankCorrelationIdIsRejectedAtTheEdge() {
        CustomerApiFacade api = new CustomerApiFacade(realService());

        assertThrows(IllegalArgumentException.class, () -> api.getById("CUS-1001", "  "));
        assertThrows(IllegalArgumentException.class, () -> api.create(amina(), null));
    }

    private static ErrorResponse failure(ApiResult result) {
        return assertInstanceOf(ApiResult.Fail.class, result).error();
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
