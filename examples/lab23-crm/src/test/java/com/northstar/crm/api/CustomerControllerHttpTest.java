package com.northstar.crm.api;

import com.northstar.crm.model.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomerControllerHttpTest {
  @LocalServerPort int port;
  @Autowired TestRestTemplate rest;

  @Test
  void createAndGetCus1001() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-Correlation-Id", "lab-request-001");
    headers.setContentType(MediaType.APPLICATION_JSON);
    Customer body = new Customer("CUS-1001", "Amina Khan", "amina.khan@example.com", "ACTIVE");

    var created = rest.postForEntity(
        "http://localhost:" + port + "/api/customers",
        new HttpEntity<>(body, headers),
        Customer.class);

    assertEquals(HttpStatus.CREATED, created.getStatusCode());
    assertEquals("CUS-1001",
        rest.getForEntity("http://localhost:" + port + "/api/customers/CUS-1001", Customer.class)
            .getBody().getId());
  }
}
