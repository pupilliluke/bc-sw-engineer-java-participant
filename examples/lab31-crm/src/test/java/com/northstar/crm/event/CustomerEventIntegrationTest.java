package com.northstar.crm.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"crm.customer-events.v1"})
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "crm.kafka.customer-events-topic=crm.customer-events.v1"
})
class CustomerEventIntegrationTest {

  @Autowired
  CustomerEventPublisher publisher;

  @Autowired
  CustomerEventListener listener;

  private static CustomerEvent aminaCreated(String eventId) {
    return new CustomerEvent(
        eventId,
        "CustomerCreated",
        1,
        Instant.parse("2026-07-13T06:00:00Z"),
        "CUS-1001",
        "lab-request-001",
        "customer-service",
        new CustomerEvent.CustomerData("Amina Khan", "ACTIVE", null, null));
  }

  @Test
  void publishesAndConsumesCustomerCreated() {
    CustomerEvent created = aminaCreated("11111111-1111-4111-8111-111111111111");

    publisher.publish(created);

    await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
        assertThat(listener.events())
            .extracting(CustomerEvent::eventId)
            .contains(created.eventId()));
  }

  @Test
  void publishesAndConsumesRaviKeyedSeparately() {
    CustomerEvent created = new CustomerEvent(
        "44444444-4444-4444-8444-444444444444",
        "CustomerCreated",
        1,
        Instant.parse("2026-07-13T06:10:00Z"),
        "CUS-1002",
        "lab-request-001",
        "customer-service",
        new CustomerEvent.CustomerData("Ravi Singh", "PROSPECT", null, null));

    publisher.publish(created);

    await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
        assertThat(listener.events())
            .filteredOn(e -> e.eventId().equals(created.eventId()))
            .singleElement()
            .satisfies(e -> {
              assertThat(e.customerId()).isEqualTo("CUS-1002");
              assertThat(e.data().fullName()).isEqualTo("Ravi Singh");
            }));
  }

  @Test
  void ignoresDuplicateEventId() {
    CustomerEvent created = aminaCreated("22222222-2222-4222-8222-222222222222");

    publisher.publish(created);
    publisher.publish(created);

    await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
        assertThat(listener.events())
            .filteredOn(e -> e.eventId().equals(created.eventId()))
            .hasSize(1));

    assertThat(listener.events())
        .filteredOn(e -> e.eventId().equals(created.eventId()))
        .hasSize(1);
  }
}
