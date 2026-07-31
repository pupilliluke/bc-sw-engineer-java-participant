package com.northstar.crm.service;

import com.northstar.crm.entity.Customer;
import com.northstar.crm.entity.CustomerStatus;
import com.northstar.crm.exception.BusinessException;
import com.northstar.crm.repository.CustomerRepository;
import com.northstar.crm.repository.InMemoryCustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultCustomerServiceTest {

    private static final String CORRELATION_ID = "lab-request-001";

    private CustomerRepository repo;
    private CustomerService service;

    @BeforeEach
    void setUp() {
        repo = new InMemoryCustomerRepository();
        service = new DefaultCustomerService(repo, new CustomerValidator(repo));
    }

    @Test
    void addCustomerStampsTimestampsAndIsFoundById() {
        Customer created = service.addCustomer(amina(), CORRELATION_ID);

        assertNotNull(created.getCreatedAt(), "createdAt is stamped by the service");
        assertNotNull(created.getUpdatedAt());
        assertEquals("Amina Khan", service.findById("CUS-1001").orElseThrow().getFullName());
    }

    @Test
    void missingStatusDefaultsToProspect() {
        Customer created = service.addCustomer(new Customer("CUS-1005", "Sam Okafor",
                "sam.okafor@example.com", null, null, null), CORRELATION_ID);

        assertEquals(CustomerStatus.PROSPECT, created.getStatus());
    }

    @Test
    void activateRaviMovesProspectToActive() {
        service.addCustomer(ravi(), CORRELATION_ID);

        Customer activated = service.changeStatus("CUS-1002", CustomerStatus.ACTIVE, CORRELATION_ID);

        assertEquals(CustomerStatus.ACTIVE, activated.getStatus());
        assertEquals(CustomerStatus.ACTIVE, repo.findById("CUS-1002").orElseThrow().getStatus());
    }

    @Test
    void illegalTransitionLeavesStoredStatusUnchanged() {
        service.addCustomer(amina(), CORRELATION_ID);

        assertThrows(BusinessException.class, () -> service.changeStatus(
                "CUS-1001", CustomerStatus.PROSPECT, CORRELATION_ID));

        assertEquals(CustomerStatus.ACTIVE, repo.findById("CUS-1001").orElseThrow().getStatus(),
                "the stored status must be unchanged after a rejected transition");
    }

    @Test
    void unknownIdIsA404CarryingTheCorrelationId() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.changeStatus("CUS-9999", CustomerStatus.ACTIVE, CORRELATION_ID));

        assertEquals(404, ex.getStatusHint());
        assertEquals("CUSTOMER_NOT_FOUND", ex.getCode());
        assertTrue(ex.getMessage().contains("CUS-9999"), ex.getMessage());
        assertEquals(CORRELATION_ID, ex.getCorrelationId());
    }

    @Test
    void duplicateIdIsA409() {
        service.addCustomer(ravi(), CORRELATION_ID);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.addCustomer(new Customer("CUS-1002", "Other Ravi",
                        "other@example.com", null, CustomerStatus.PROSPECT, null), CORRELATION_ID));

        assertEquals(409, ex.getStatusHint());
    }

    @Test
    void duplicateEmailIsRejectedAcrossTheSharedRepository() {
        service.addCustomer(amina(), CORRELATION_ID);

        assertThrows(BusinessException.class, () -> service.addCustomer(
                new Customer("CUS-1006", "Amina Duplicate", "amina.khan@example.com",
                        null, CustomerStatus.PROSPECT, null), CORRELATION_ID));
    }

    @Test
    void listAllCannotBeMutatedByCallers() {
        service.addCustomer(amina(), CORRELATION_ID);

        assertThrows(UnsupportedOperationException.class, () -> service.listAll().clear());
        assertEquals(1, service.listAll().size());
    }

    @Test
    void lookupByEqualValueNotSameReference() {
        service.addCustomer(amina(), CORRELATION_ID);

        String idFromElsewhere = new String("CUS-1001");

        assertEquals("Amina Khan", service.findById(idFromElsewhere).orElseThrow().getFullName());
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
