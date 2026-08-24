package com.northstar.crm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.northstar.crm.api.CreateCustomerRequest;
import com.northstar.crm.api.DuplicateCustomerException;
import com.northstar.crm.repository.CustomerEntity;
import com.northstar.crm.repository.CustomerRepository;
import com.northstar.crm.service.CustomerMapper;
import com.northstar.crm.service.CustomerService;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * The lab 25 version of this test drove the service through
 * InMemoryCustomerRepository, which module 39 deletes in favour of real
 * persistence. The transition rules it covered are still business logic worth
 * testing without a database, so the repository is mocked instead. Everything
 * that needs PostgreSQL lives in CustomerRepositoryIT.
 */
class CustomerServiceTest {

  private static final String CORRELATION = "lab-request-001";

  private CustomerRepository repository;
  private CustomerService service;

  private static CustomerEntity entity(String publicId, String name, String email, String status) {
    CustomerEntity e = new CustomerEntity();
    e.setPublicId(publicId);
    e.setFullName(name);
    e.setEmail(email);
    e.setStatus(status);
    e.setCreatedAt(Instant.now());
    return e;
  }

  @BeforeEach
  void setUp() {
    repository = Mockito.mock(CustomerRepository.class);
    service = new CustomerService(repository, new CustomerMapper());
    when(repository.save(any(CustomerEntity.class))).thenAnswer(i -> i.getArgument(0));
  }

  private void existing(String publicId, String name, String email, String status) {
    when(repository.findByPublicId(publicId))
        .thenReturn(Optional.of(entity(publicId, name, email, status)));
  }

  @Test
  void getSeededCus1001() {
    existing("CUS-1001", "Amina Khan", "amina@example.com", "ACTIVE");

    var amina = service.get("CUS-1001");

    assertEquals("CUS-1001", amina.id());
    assertEquals("Amina Khan", amina.name());
    assertEquals("ACTIVE", amina.status());
  }

  @Test
  void duplicateCreateRejected() {
    when(repository.existsByEmail("amina@example.com")).thenReturn(true);

    assertThrows(
        DuplicateCustomerException.class,
        () -> service.create(
            new CreateCustomerRequest("CUS-1001", "Amina Khan", "amina@example.com", "ACTIVE"),
            CORRELATION));
    verify(repository, never()).save(any(CustomerEntity.class));
  }

  @Test
  void emailIsNormalizedBeforeTheDuplicateCheck() {
    when(repository.existsByEmail("amina@example.com")).thenReturn(true);

    assertThrows(
        DuplicateCustomerException.class,
        () -> service.create(
            new CreateCustomerRequest("CUS-1009", "Amina Khan", "  AMINA@Example.COM ", "ACTIVE"),
            CORRELATION));
  }

  @Test
  void activateRaviFromProspect() {
    existing("CUS-1002", "Ravi Singh", "ravi@example.com", "PROSPECT");

    var ravi = service.updateStatus("CUS-1002", "ACTIVE", CORRELATION);

    assertEquals("ACTIVE", ravi.status());
  }

  @Test
  void illegalTransitionRejectedAndStatusUnchanged() {
    existing("CUS-1001", "Amina Khan", "amina@example.com", "ACTIVE");

    assertThrows(
        IllegalStateException.class,
        () -> service.updateStatus("CUS-1001", "PROSPECT", CORRELATION));
    verify(repository, never()).save(any(CustomerEntity.class));
  }

  @Test
  void closedIsTerminal() {
    existing("CUS-1003", "Closed Customer", "closed@example.com", "CLOSED");

    assertThrows(
        IllegalStateException.class,
        () -> service.updateStatus("CUS-1003", "ACTIVE", CORRELATION));
  }

  @Test
  void missingCustomerIsNotFound() {
    when(repository.findByPublicId(anyString())).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> service.get("CUS-9999"));
  }
}
