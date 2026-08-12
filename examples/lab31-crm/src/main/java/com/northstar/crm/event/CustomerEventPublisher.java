package com.northstar.crm.event;

import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Component
public class CustomerEventPublisher {

  private static final Logger log = LoggerFactory.getLogger(CustomerEventPublisher.class);

  private final KafkaTemplate<String, CustomerEvent> kafkaTemplate;
  private final String topic;

  public CustomerEventPublisher(
      KafkaTemplate<String, CustomerEvent> kafkaTemplate,
      @Value("${crm.kafka.customer-events-topic}") String topic) {
    this.kafkaTemplate = kafkaTemplate;
    this.topic = topic;
  }

  public CompletableFuture<SendResult<String, CustomerEvent>> publish(CustomerEvent event) {
    return kafkaTemplate.send(topic, event.customerId(), event)
        .whenComplete((result, error) -> {
          if (error != null) {
            log.error("customer_event_publish_failed id={} correlationId={}",
                event.eventId(), event.correlationId(), error);
          } else {
            log.info("customer_event_published id={} correlationId={} partition={} offset={}",
                event.eventId(),
                event.correlationId(),
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset());
          }
        });
  }
}
