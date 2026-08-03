package com.northstar.crm.service;

import com.northstar.crm.entity.Customer;
import com.northstar.crm.entity.CustomerStatus;
import com.northstar.crm.exception.BusinessException;
import com.northstar.crm.repository.CustomerRepository;
import com.northstar.crm.repository.InMemoryCustomerRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Lab 16's DefaultCustomerServiceTest, renamed to the class the Lab 17 guide
 * names and extended with the paths the coverage gate exposed.
 *
 * Collaborators are real: a fresh InMemoryCustomerRepository and a
 * CustomerValidator built on it, wired in @BeforeEach so no test can see
 * another test's data. Substituting them with mocks is Lab 18.
 */
class CustomerServiceTests {

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
    void addCustomerKeepsTheStatusTheCallerSupplied() {
        Customer created = service.addCustomer(amina(), CORRELATION_ID);

        assertEquals(CustomerStatus.ACTIVE, created.getStatus());
        assertEquals(CustomerStatus.ACTIVE, repo.findById("CUS-1001").orElseThrow().getStatus());
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

        assertEquals("CUS-1002", activated.getCustomerId());
        assertEquals(CustomerStatus.ACTIVE, activated.getStatus());
        assertEquals(CustomerStatus.ACTIVE, repo.findById("CUS-1002").orElseThrow().getStatus());
    }

    @Test
    void activationMovesUpdatedAtWithoutTouchingCreatedAt() {
        Customer created = service.addCustomer(ravi(), CORRELATION_ID);
        var createdAt = created.getCreatedAt().minusMinutes(5);
        created.setCreatedAt(createdAt);
        created.setUpdatedAt(createdAt);

        Customer activated = service.changeStatus("CUS-1002", CustomerStatus.ACTIVE, CORRELATION_ID);

        assertEquals(createdAt, activated.getCreatedAt());
        assertTrue(activated.getUpdatedAt().isAfter(createdAt),
                "updatedAt is restamped on the transition");
    }

    @Test
    void illegalTransitionLeavesStoredStatusUnchanged() {
        service.addCustomer(amina(), CORRELATION_ID);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.changeStatus(
                "CUS-1001", CustomerStatus.PROSPECT, CORRELATION_ID));

        assertEquals(409, ex.getStatusHint());
        assertEquals("BUSINESS_CONFLICT", ex.getCode());
        assertEquals(CORRELATION_ID, ex.getCorrelationId());
        assertEquals(CustomerStatus.ACTIVE, repo.findById("CUS-1001").orElseThrow().getStatus(),
                "the stored status must be unchanged after a rejected transition");
    }

    @Test
    void activatingAnAlreadyActiveCustomerIsRejected() {
        service.addCustomer(amina(), CORRELATION_ID);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.changeStatus(
                "CUS-1001", CustomerStatus.ACTIVE, CORRELATION_ID));

        assertEquals(409, ex.getStatusHint());
        assertTrue(ex.getMessage().contains("ACTIVE -> ACTIVE"), ex.getMessage());
    }

    @Test
    void aMissingNewStatusIsA400NotAConflict() {
        service.addCustomer(ravi(), CORRELATION_ID);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.changeStatus("CUS-1002", null, CORRELATION_ID));

        assertEquals(400, ex.getStatusHint());
        assertEquals("VALIDATION_FAILED", ex.getCode());
        assertEquals(CustomerStatus.PROSPECT, repo.findById("CUS-1002").orElseThrow().getStatus());
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
    void findingAnUnknownIdIsAnEmptyOptionalNotAnException() {
        service.addCustomer(amina(), CORRELATION_ID);

        assertEquals(Optional.empty(), service.findById("CUS-9999"));
    }

    @Test
    void duplicateIdIsA409() {
        service.addCustomer(ravi(), CORRELATION_ID);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.addCustomer(new Customer("CUS-1002", "Other Ravi",
                        "other@example.com", null, CustomerStatus.PROSPECT, null), CORRELATION_ID));

        assertEquals(409, ex.getStatusHint());
        assertEquals(1, service.listAll().size(), "the rejected create stored nothing");
    }

    @Test
    void duplicateEmailIsRejectedAcrossTheSharedRepository() {
        service.addCustomer(amina(), CORRELATION_ID);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.addCustomer(
                new Customer("CUS-1006", "Amina Duplicate", "amina.khan@example.com",
                        null, CustomerStatus.PROSPECT, null), CORRELATION_ID));

        assertEquals(409, ex.getStatusHint());
        assertTrue(ex.getMessage().contains("already registered"), ex.getMessage());
    }

    @Test
    void listAllReturnsEveryStoredCustomer() {
        service.addCustomer(amina(), CORRELATION_ID);
        service.addCustomer(ravi(), CORRELATION_ID);

        assertEquals(2, service.listAll().size());
        assertTrue(service.listAll().stream()
                .anyMatch(customer -> "CUS-1002".equals(customer.getCustomerId())));
    }

    @Test
    void listAllIsEmptyBeforeAnythingIsCreated() {
        assertTrue(service.listAll().isEmpty());
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
