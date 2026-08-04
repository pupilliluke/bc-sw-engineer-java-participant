package com.northstar.crm.integration;

import com.northstar.crm.model.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * HTTP integration tests against a real server on a random port. The store is
 * the seeded in-memory repository, Amina CUS-1001 and Ravi CUS-1002; created
 * ids stay away from the seeds so no test overwrites a fixture.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomerApiIT {

    private static final String CORRELATION_ID = "lab-request-001";

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Test
    void getAminaReturns200() {
        ResponseEntity<Customer> got = rest.getForEntity("/api/customers/CUS-1001", Customer.class);

        assertEquals(HttpStatus.OK, got.getStatusCode());
        assertEquals("CUS-1001", got.getBody().getCustomerId());
        assertEquals("Amina Khan", got.getBody().getFullName());
        assertEquals("ACTIVE", got.getBody().getStatus());
    }

    @Test
    void createEchoesCorrelationHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Correlation-Id", CORRELATION_ID);
        Map<String, String> body = Map.of(
                "customerId", "CUS-1005",
                "fullName", "Sam Okafor",
                "email", "sam.okafor@example.com",
                "status", "ACTIVE");

        ResponseEntity<Customer> created = rest.exchange("/api/customers", HttpMethod.POST,
                new HttpEntity<>(body, headers), Customer.class);

        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        assertEquals(CORRELATION_ID, created.getHeaders().getFirst("X-Correlation-Id"));
        assertEquals("CUS-1005", created.getBody().getCustomerId());

        ResponseEntity<Customer> got = rest.getForEntity("/api/customers/CUS-1005", Customer.class);
        assertEquals(HttpStatus.OK, got.getStatusCode());
        assertEquals("Sam Okafor", got.getBody().getFullName());
    }

    @Test
    void missingCustomerReturns404() {
        ResponseEntity<String> got = rest.getForEntity("/api/customers/CUS-9999", String.class);

        assertEquals(HttpStatus.NOT_FOUND, got.getStatusCode());
    }

    @Test
    void blankFullNameReturns400() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Correlation-Id", CORRELATION_ID);
        Map<String, String> body = Map.of(
                "customerId", "CUS-1002",
                "fullName", "",
                "email", "ravi.singh@example.com",
                "status", "PROSPECT");

        ResponseEntity<String> response = rest.exchange("/api/customers", HttpMethod.POST,
                new HttpEntity<>(body, headers), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("fullName"), response.getBody());

        ResponseEntity<Customer> ravi = rest.getForEntity("/api/customers/CUS-1002", Customer.class);
        assertEquals("Ravi Singh", ravi.getBody().getFullName(),
                "the rejected create must not overwrite the stored Ravi");
    }
}
