package com.northstar.crm;

import com.northstar.crm.model.Customer;
import com.northstar.crm.repository.InMemoryCustomerRepository;
import com.northstar.crm.service.CustomerService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomerServiceTest {
  @Test
  void getSeededCus1001() {
    CustomerService service = new CustomerService(new InMemoryCustomerRepository());
    Customer amina = service.get("CUS-1001");
    assertEquals("CUS-1001", amina.getId());
    assertEquals("Amina Khan", amina.getName());
    assertEquals("ACTIVE", amina.getStatus());
  }

  @Test
  void duplicateCreateRejected() {
    CustomerService service = new CustomerService(new InMemoryCustomerRepository());
    assertThrows(IllegalStateException.class,
        () -> service.create(Customer.amina(), "lab-request-001"));
  }

  @Test
  void activateRaviFromProspect() {
    CustomerService service = new CustomerService(new InMemoryCustomerRepository());
    Customer ravi = service.updateStatus("CUS-1002", "ACTIVE", "lab-request-001");
    assertEquals("ACTIVE", ravi.getStatus());
    assertEquals("ACTIVE", service.get("CUS-1002").getStatus());
  }

  @Test
  void illegalTransitionRejectedAndStatusUnchanged() {
    CustomerService service = new CustomerService(new InMemoryCustomerRepository());
    assertThrows(IllegalStateException.class,
        () -> service.updateStatus("CUS-1001", "PROSPECT", "lab-request-001"));
    assertEquals("ACTIVE", service.get("CUS-1001").getStatus());
  }

  @Test
  void closedIsTerminal() {
    CustomerService service = new CustomerService(new InMemoryCustomerRepository());
    service.updateStatus("CUS-1002", "CLOSED", "lab-request-001");
    assertThrows(IllegalStateException.class,
        () -> service.updateStatus("CUS-1002", "ACTIVE", "lab-request-001"));
    assertEquals("CLOSED", service.get("CUS-1002").getStatus());
  }

  @Test
  void listReturnsSeedsAndCreated() {
    CustomerService service = new CustomerService(new InMemoryCustomerRepository());
    assertEquals(2, service.list().size());
    service.create(new Customer("CUS-1003", "Maya Chen", "maya@example.com", "PROSPECT"),
        "lab-request-001");
    assertEquals(3, service.list().size());
  }
}
