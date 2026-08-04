package com.northstar.crm.logging;

import com.northstar.crm.model.Customer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The PII rule as a test rather than a README line. Both cases assert on the
 * console the operator would search, so a log line that leaks a name or an
 * address fails the build instead of reaching a log store.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(OutputCaptureExtension.class)
class CustomerLoggingIT {

    private static final String CORRELATION_ID = "lab-request-001";

    @Autowired
    TestRestTemplate rest;

    @Test
    void createLogsIdsNotPii(CapturedOutput output) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Correlation-Id", CORRELATION_ID);
        Map<String, String> body = Map.of(
                "customerId", "CUS-1006",
                "fullName", "Dana Whitfield",
                "email", "dana.whitfield@example.com",
                "status", "ACTIVE");

        ResponseEntity<Customer> created = rest.exchange("/api/customers", HttpMethod.POST,
                new HttpEntity<>(body, headers), Customer.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(output.getOut()).contains("CUS-1006");
        assertThat(output.getOut()).contains(CORRELATION_ID);
        assertThat(output.getOut()).contains("op=customer.create");
        assertThat(output.getOut()).doesNotContain("Dana Whitfield");
        assertThat(output.getOut()).doesNotContain("@example.com");
    }

    @Test
    void getLogsIdsNotPii(CapturedOutput output) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Correlation-Id", CORRELATION_ID);

        ResponseEntity<Customer> got = rest.exchange("/api/customers/CUS-1001", HttpMethod.GET,
                new HttpEntity<>(headers), Customer.class);

        assertThat(got.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(got.getBody().getFullName()).isEqualTo("Amina Khan");
        assertThat(output.getOut()).contains("cust=CUS-1001");
        assertThat(output.getOut()).contains("op=customer.get");
        assertThat(output.getOut()).contains(CORRELATION_ID);
        assertThat(output.getOut()).doesNotContain("Amina");
        assertThat(output.getOut()).doesNotContain("@example.com");
    }
}
