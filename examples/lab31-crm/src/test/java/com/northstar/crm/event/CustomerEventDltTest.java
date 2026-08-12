package com.northstar.crm.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

/** Failure experiment 2: key does not match data.customerId, non-retryable, lands on the DLQ. */
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"crm.customer-events.v1", "crm.customer-events.v1.dlq"})
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "crm.kafka.customer-events-topic=crm.customer-events.v1"
})
class CustomerEventDltTest {

  @Autowired
  KafkaTemplate<String, CustomerEvent> template;

  @Autowired
  EmbeddedKafkaBroker broker;

  @Autowired
  CustomerEventListener listener;

  @Test
  void keyMismatchGoesToDeadLetterTopic() {
    CustomerEvent event = new CustomerEvent(
        "33333333-3333-4333-8333-333333333333",
        "CustomerCreated",
        1,
        Instant.parse("2026-07-13T06:00:00Z"),
        "CUS-1001",
        "lab-request-001",
        "customer-service",
        new CustomerEvent.CustomerData("Amina Khan", "ACTIVE", null, null));

    // Deliberately wrong key. The publisher cannot do this, it always keys by
    // customerId, so the record is sent through the template directly.
    template.send("crm.customer-events.v1", "CUS-9999", event);

    Map<String, Object> props =
        KafkaTestUtils.consumerProps("dlq-test-reader", "false", broker);
    try (Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<>(
        props, new StringDeserializer(), new StringDeserializer()).createConsumer()) {

      consumer.subscribe(java.util.List.of("crm.customer-events.v1.dlq"));
      ConsumerRecord<String, String> dead =
          KafkaTestUtils.getSingleRecord(consumer, "crm.customer-events.v1.dlq",
              Duration.ofSeconds(20));

      assertThat(dead.key()).isEqualTo("CUS-9999");
      assertThat(header(dead, KafkaHeaders.DLT_ORIGINAL_TOPIC))
          .isEqualTo("crm.customer-events.v1");
      // Spring wraps a listener throw, so the top-level header names the wrapper
      // and the cause header is the one that names the contract exception.
      assertThat(header(dead, KafkaHeaders.DLT_EXCEPTION_FQCN))
          .isEqualTo("org.springframework.kafka.listener.ListenerExecutionFailedException");
      assertThat(header(dead, KafkaHeaders.DLT_EXCEPTION_CAUSE_FQCN))
          .isEqualTo(InvalidCustomerEventException.class.getName());
      assertThat(header(dead, KafkaHeaders.DLT_ORIGINAL_PARTITION)).isNotNull();
      assertThat(header(dead, KafkaHeaders.DLT_ORIGINAL_OFFSET)).isNotNull();
    }

    // The side effect never ran for the rejected record.
    assertThat(listener.events())
        .extracting(CustomerEvent::eventId)
        .doesNotContain(event.eventId());
  }

  private static String header(ConsumerRecord<String, String> record, String name) {
    var h = record.headers().lastHeader(name);
    return h == null ? null : new String(h.value());
  }
}
