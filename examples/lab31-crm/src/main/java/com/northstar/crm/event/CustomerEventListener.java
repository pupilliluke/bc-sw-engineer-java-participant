package com.northstar.crm.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class CustomerEventListener {

  private static final Logger log = LoggerFactory.getLogger(CustomerEventListener.class);
  private final ProcessedEventStore store;
  private final List<CustomerEvent> handled = new CopyOnWriteArrayList<>();

  public CustomerEventListener(ProcessedEventStore store) {
    this.store = store;
  }

  @KafkaListener(topics = "${crm.kafka.customer-events-topic}")
  public void onCustomerEvent(
      @Payload CustomerEvent event,
      @Header(KafkaHeaders.RECEIVED_KEY) String key) {
      if(key == null || !key.equals(event.customerId())) {
          throw new InvalidCustomerEventException(
              "Invalid key: " + key + ", expected: " + event.customerId());
      }
      if(!store.markIfNew(event.eventId())) {
          log.info("duplicate_event_ignored id={}", event.eventId());
          return;
      }
      log.info("Correlation ID : {}, Customer ID : {}", event.correlationId(), event.customerId());
      handled.add(event);
  }

  /** Events whose side effect actually ran. Duplicates are not added twice. */
  public List<CustomerEvent> events() {
    return List.copyOf(handled);
  }
}
