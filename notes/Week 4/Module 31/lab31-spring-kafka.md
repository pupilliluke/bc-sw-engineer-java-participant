# Lab 31 — Spring Kafka Roles

## Reference

| Kafka idea | Spring Boot piece |
| --- | --- |
| Produce record | KafkaTemplate.send(...) |
| Consume record | @KafkaListener |
| Bootstrap servers | spring.kafka.bootstrap-servers |
| Group id | spring.kafka.consumer.group-id |

## Step 2 — CRM story

Write: after HTTP creates Amina, service calls `KafkaTemplate` to `crm.customer-events.v1` with key `CUS-1001`.
    
    KafkaTemplate.send("crm.customer-events.v1", "CUS-1001", customerCreatedEvent);

In lab 30 the producer built Properties, a KafkaProducer and a ProducerRecord by
hand and called send().get(). In Spring that is an injected KafkaTemplate and
one send call with the topic, the key and the event. acks=all and
enable.idempotence move out of code into application.yml under
spring.kafka.producer. The key is still customerId, so every event for Amina
lands on the same partition and keeps its order.

## Step 3 — Listener story

Write: notifications listener uses group `crm-notifications` and processes the JSON envelope.

    @KafkaListener(topics = "crm.customer-events.v1", groupId = "crm-notifications")

The console consumer run with --group crm-notifications becomes an annotated
method, and Spring runs the poll loop and the offset commits instead of the CLI.
Group behaviour is the same as lab 30, members of crm-notifications split the
three partitions between them and crm-audit reads the same records
independently under its own offsets. The method receives the JSON envelope as
its payload rather than a printed line.

## Step 4 — Gap check

List one question you still have about serializers (String/JSON) before lab.

    What determines how to serialize the data? Is there a config?

Lab 30 set KEY_SERIALIZER_CLASS_CONFIG and VALUE_SERIALIZER_CLASS_CONFIG in
code and used StringSerializer for both, and the value was JSON text read out of
a file. In Spring those are application.yml properties. If the value serializer
is JsonSerializer instead, does the consumer need the producer's class to
deserialize, or does the type come from a header, and does that couple the two
applications' package names?

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab31-spring-kafka.md`
- [ x ] Table copied
- [ x ] Produce + listen stories written
- [ x ] One serializer question listed

