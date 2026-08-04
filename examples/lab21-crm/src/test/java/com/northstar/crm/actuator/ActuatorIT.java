package com.northstar.crm.actuator;

import com.northstar.crm.health.CrmReadinessIndicator;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ActuatorIT {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Autowired
    CrmReadinessIndicator readiness;

    @Test
    void healthAndProbesAreUp() {
        ResponseEntity<String> health = rest.getForEntity("/actuator/health", String.class);
        ResponseEntity<String> liveness = rest.getForEntity("/actuator/health/liveness", String.class);
        ResponseEntity<String> readinessResponse = rest.getForEntity("/actuator/health/readiness", String.class);

        assertEquals(HttpStatus.OK, health.getStatusCode());
        assertEquals(HttpStatus.OK, liveness.getStatusCode());
        assertEquals(HttpStatus.OK, readinessResponse.getStatusCode());
        assertTrue(health.getBody().contains("crmReadinessIndicator"), health.getBody());
    }

    @Test
    void readinessCanGoDownWhileLivenessStaysUp() {
        readiness.setReady(false);
        try {
            ResponseEntity<String> down = rest.getForEntity("/actuator/health/readiness", String.class);
            ResponseEntity<String> liveness = rest.getForEntity("/actuator/health/liveness", String.class);

            assertEquals(HttpStatus.SERVICE_UNAVAILABLE, down.getStatusCode());
            assertTrue(down.getBody().contains("OUT_OF_SERVICE"), down.getBody());
            assertEquals(HttpStatus.OK, liveness.getStatusCode());
            assertTrue(liveness.getBody().contains("UP"), liveness.getBody());
        } finally {
            readiness.setReady(true);
        }

        ResponseEntity<String> restored = rest.getForEntity("/actuator/health/readiness", String.class);
        assertEquals(HttpStatus.OK, restored.getStatusCode());
    }

    @Test
    void createMetricAppearsAfterTraffic() {
        ResponseEntity<String> created = rest.postForEntity("/api/customers",
                Map.of("customerId", "CUS-2101", "fullName", "Metric User", "status", "PROSPECT"),
                String.class);
        assertEquals(HttpStatus.CREATED, created.getStatusCode());

        ResponseEntity<String> metric = rest.getForEntity("/actuator/metrics/crm.customer.create", String.class);

        assertEquals(HttpStatus.OK, metric.getStatusCode());
        assertTrue(metric.getBody().contains("crm.customer.create"), metric.getBody());
        assertTrue(metric.getBody().contains("COUNT"), metric.getBody());
        assertTrue(metric.getBody().contains("result"), metric.getBody());
    }
}
