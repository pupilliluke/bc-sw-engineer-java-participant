package com.northstar.crm.service;

import com.northstar.crm.entity.Customer;
import com.northstar.crm.entity.CustomerStatus;
import com.northstar.crm.exception.BusinessException;
import com.northstar.crm.repository.CustomerRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;

/**
 * The same three paths as CustomerServiceMockitoTest in BDDMockito wording.
 * given/willReturn is when/thenReturn and then().should() is verify(); the
 * engine, the strictness and the failure messages are identical.
 *
 * One style per class. Mixing the Mockito and BDDMockito static imports gives
 * a file where half the arrange reads backwards.
 */
@ExtendWith(MockitoExtension.class)
class CustomerServiceBddMockTest {

    private static final String CORRELATION_ID = "lab-request-001";

    @Mock
    private CustomerRepository repository;

    private DefaultCustomerService service;

    @BeforeEach
    void setUp() {
        service = new DefaultCustomerService(repository, new CustomerValidator(repository));
    }

    @Test
    void activatesRaviInBddStyle() {
        given(repository.findById("CUS-1002")).willReturn(Optional.of(ravi()));
        given(repository.save(any(Customer.class))).willAnswer(call -> call.getArgument(0));

        Customer updated = service.changeStatus("CUS-1002", CustomerStatus.ACTIVE, CORRELATION_ID);

        then(repository).should().findById("CUS-1002");
        then(repository).should().save(any(Customer.class));
        assertEquals(CustomerStatus.ACTIVE, updated.getStatus());
    }

    @Test
    void unknownIdNeverSavesInBddStyle() {
        given(repository.findById("CUS-9999")).willReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.changeStatus("CUS-9999", CustomerStatus.ACTIVE, CORRELATION_ID));

        assertEquals(404, ex.getStatusHint());
        assertEquals(CORRELATION_ID, ex.getCorrelationId());
        then(repository).should().findById("CUS-9999");
        then(repository).should(never()).save(any(Customer.class));
    }

    @Test
    void addingAminaCapturesTheSavedCustomerInBddStyle() {
        given(repository.save(any(Customer.class))).willAnswer(call -> call.getArgument(0));

        service.addCustomer(amina(), CORRELATION_ID);

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        then(repository).should().save(captor.capture());
        assertEquals("CUS-1001", captor.getValue().getCustomerId());
        assertEquals(CustomerStatus.ACTIVE, captor.getValue().getStatus());
    }

    private static Customer amina() {
        return new Customer("CUS-1001", "Amina Khan", "amina.khan@example.com",
                "555-0101", CustomerStatus.ACTIVE, null);
    }

    private static Customer ravi() {
        return new Customer("CUS-1002", "Ravi Singh", "ravi.singh@example.com",
                "555-0102", CustomerStatus.PROSPECT, null);
    }
}
