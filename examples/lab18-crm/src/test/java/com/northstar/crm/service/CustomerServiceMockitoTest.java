package com.northstar.crm.service;

import com.northstar.crm.entity.Customer;
import com.northstar.crm.entity.CustomerStatus;
import com.northstar.crm.exception.BusinessException;
import com.northstar.crm.repository.CustomerRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * The Lab 18 unit suite. CustomerRepository is a Mockito mock, so no HashMap
 * decides what these tests read, and every write is a verifiable call rather
 * than a stored value read back.
 *
 * CustomerValidator stays real and is built on the same mock, which is the
 * wiring production uses. It holds the transition table these tests exist to
 * check, so stubbing it would let a wrong transition pass.
 *
 * Lab 17's CustomerServiceTests still runs the same paths against the real
 * InMemoryCustomerRepository; docs/isolation-policy.md says why both stay.
 */
@ExtendWith(MockitoExtension.class)
class CustomerServiceMockitoTest {

    private static final String CORRELATION_ID = "lab-request-001";

    @Mock
    private CustomerRepository repository;

    private DefaultCustomerService service;

    @BeforeEach
    void setUp() {
        service = new DefaultCustomerService(repository, new CustomerValidator(repository));
    }

    @Test
    void activatesProspectUsingStubbedRepository() {
        when(repository.findById("CUS-1002")).thenReturn(Optional.of(ravi()));
        when(repository.save(any(Customer.class))).thenAnswer(call -> call.getArgument(0));

        Customer activated = service.changeStatus("CUS-1002", CustomerStatus.ACTIVE, CORRELATION_ID);

        assertEquals(CustomerStatus.ACTIVE, activated.getStatus());
        verify(repository).findById("CUS-1002");
        verify(repository).save(argThat(customer -> "CUS-1002".equals(customer.getCustomerId())
                && customer.getStatus() == CustomerStatus.ACTIVE));
    }

    /**
     * save(ravi) would also pass, because Customer.equals compares customerId
     * alone. The captor is what reads the status the argument carried.
     */
    @Test
    void activationCapturesTheCustomerThatCrossedThePort() {
        when(repository.findById("CUS-1002")).thenReturn(Optional.of(ravi()));
        when(repository.save(any(Customer.class))).thenAnswer(call -> call.getArgument(0));

        service.changeStatus("CUS-1002", CustomerStatus.ACTIVE, CORRELATION_ID);

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(repository).save(captor.capture());
        assertEquals("CUS-1002", captor.getValue().getCustomerId());
        assertEquals(CustomerStatus.ACTIVE, captor.getValue().getStatus());
        assertNotNull(captor.getValue().getUpdatedAt(), "updatedAt is restamped before the write");
    }

    /**
     * The read has to happen first, because the status the validator compares
     * against is the stored one.
     */
    @Test
    void activationReadsThroughThePortBeforeItWrites() {
        when(repository.findById("CUS-1002")).thenReturn(Optional.of(ravi()));
        when(repository.save(any(Customer.class))).thenAnswer(call -> call.getArgument(0));

        service.changeStatus("CUS-1002", CustomerStatus.ACTIVE, CORRELATION_ID);

        InOrder order = inOrder(repository);
        order.verify(repository).findById("CUS-1002");
        order.verify(repository).save(any(Customer.class));
        verifyNoMoreInteractions(repository);
    }

    @Test
    void unknownIdIsA404AndNeverSaves() {
        when(repository.findById("CUS-9999")).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.changeStatus("CUS-9999", CustomerStatus.ACTIVE, CORRELATION_ID));

        assertEquals(404, ex.getStatusHint());
        assertEquals("CUSTOMER_NOT_FOUND", ex.getCode());
        assertEquals(CORRELATION_ID, ex.getCorrelationId());
        verify(repository).findById("CUS-9999");
        verify(repository, never()).save(any(Customer.class));
    }

    @Test
    void illegalTransitionIsA409AndNeverSaves() {
        when(repository.findById("CUS-1001")).thenReturn(Optional.of(amina()));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.changeStatus("CUS-1001", CustomerStatus.PROSPECT, CORRELATION_ID));

        assertEquals(409, ex.getStatusHint());
        assertEquals("BUSINESS_CONFLICT", ex.getCode());
        assertTrue(ex.getMessage().contains("ACTIVE -> PROSPECT"), ex.getMessage());
        verify(repository, never()).save(any(Customer.class));
    }

    @Test
    void aMissingNewStatusIsA400AndNeverSaves() {
        when(repository.findById("CUS-1002")).thenReturn(Optional.of(ravi()));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.changeStatus("CUS-1002", null, CORRELATION_ID));

        assertEquals(400, ex.getStatusHint());
        assertEquals("VALIDATION_FAILED", ex.getCode());
        verify(repository, never()).save(any(Customer.class));
    }

    @Test
    void addCustomerCapturesSavedEntity() {
        when(repository.existsById("CUS-1001")).thenReturn(false);
        when(repository.existsByEmail("amina.khan@example.com")).thenReturn(false);
        when(repository.save(any(Customer.class))).thenAnswer(call -> call.getArgument(0));

        service.addCustomer(amina(), CORRELATION_ID);

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(repository).save(captor.capture());
        assertEquals("CUS-1001", captor.getValue().getCustomerId());
        assertEquals("Amina Khan", captor.getValue().getFullName());
        assertEquals(CustomerStatus.ACTIVE, captor.getValue().getStatus());
        assertNotNull(captor.getValue().getCreatedAt(), "createdAt is stamped by the service");
    }

    @Test
    void aCustomerWithoutAStatusIsSavedAsProspect() {
        when(repository.save(any(Customer.class))).thenAnswer(call -> call.getArgument(0));

        service.addCustomer(new Customer("CUS-1005", "Sam Okafor",
                "sam.okafor@example.com", null, null, null), CORRELATION_ID);

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(repository).save(captor.capture());
        assertEquals(CustomerStatus.PROSPECT, captor.getValue().getStatus());
        assertEquals(captor.getValue().getCreatedAt(), captor.getValue().getUpdatedAt());
    }

    /**
     * The id rule is checked before the email rule, so a duplicate id never
     * asks the port about the address. Lab 17 could assert the rejection but
     * not the call that did not happen.
     */
    @Test
    void aDuplicateIdIsRejectedBeforeTheEmailIsLookedUp() {
        when(repository.existsById("CUS-1002")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.addCustomer(ravi(), CORRELATION_ID));

        assertEquals(409, ex.getStatusHint());
        assertTrue(ex.getMessage().contains("CUS-1002"), ex.getMessage());
        verify(repository, never()).existsByEmail(anyString());
        verify(repository, never()).save(any(Customer.class));
    }

    @Test
    void aDuplicateEmailIsA409AndNeverSaves() {
        when(repository.existsById("CUS-1001")).thenReturn(false);
        when(repository.existsByEmail("amina.khan@example.com")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.addCustomer(amina(), CORRELATION_ID));

        assertEquals(409, ex.getStatusHint());
        assertEquals(CORRELATION_ID, ex.getCorrelationId());
        assertTrue(ex.getMessage().contains("already registered"), ex.getMessage());
        assertFalse(ex.getMessage().contains("amina.khan@example.com"), ex.getMessage());
        verify(repository).existsByEmail("amina.khan@example.com");
        verify(repository, never()).save(any(Customer.class));
    }

    @Test
    void findByIdReturnsWhatThePortReturns() {
        when(repository.findById("CUS-1001")).thenReturn(Optional.of(amina()));

        assertEquals("Amina Khan", service.findById("CUS-1001").orElseThrow().getFullName());
        assertEquals(Optional.empty(), service.findById("CUS-9999"));
        verify(repository).findById("CUS-1001");
        verify(repository).findById("CUS-9999");
    }

    @Test
    void listAllCopiesWhatThePortReturns() {
        when(repository.findAll()).thenReturn(List.of(amina(), ravi()));

        List<Customer> all = service.listAll();

        assertEquals(2, all.size());
        assertThrows(UnsupportedOperationException.class, all::clear);
        verify(repository).findAll();
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
