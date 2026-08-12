package com.northstar.crm.config;

import com.northstar.crm.event.InvalidCustomerEventException;
import com.northstar.crm.event.UnsupportedEventVersionException;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaErrorConfig {

  /** Lab 30 froze this name. Spring's default would be the source topic plus ".DLT". */
  static final String DLQ_SUFFIX = ".dlq";

  @Bean
  public CommonErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> template) {
    DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
        template,
        // Partition -1 lets the broker choose. The default is the source partition
        // number, and the DLQ has 1 partition while the source has 3, so anything
        // failing on partition 1 or 2 would fail to publish.
        (record, exception) -> new TopicPartition(record.topic() + DLQ_SUFFIX, -1));

    DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2L));
    handler.addNotRetryableExceptions(
        InvalidCustomerEventException.class,
        UnsupportedEventVersionException.class,
        DeserializationException.class);
    return handler;
  }
}
