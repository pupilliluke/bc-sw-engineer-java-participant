# Lab 31 — Fill Spring Kafka TODOs

## Step 1 — Paste snippet

Create `notes/lab31-todos.md`:

```java
// application.yml ideas
spring.kafka.bootstrap-servers: localhost:9092
spring.kafka.consumer.group-id: crm-notifications

@Service
class CustomerEventPublisher {
  private final KafkaTemplate<String, String> template;
  void publishCreated(String customerId, String json) {
    // key must be CUS-1001 / CUS-1002, not a random UUID, or the customer's
    // events spread across partitions and lose their order
    template.send("crm.customer-events.v1", customerId, json); // topic
  }
}

@KafkaListener(topics = "crm.customer-events.v1", groupId = "crm-notifications")
void onEvent(String payload) { /* TODO: parse + idempotent handle */ }

// TODO Lab 31: route poison messages to crm.customer-events.v1.dlq
```

## Step 2 — Fill blanks

Fill with: `localhost:9092` (or instructor bootstrap), `crm-notifications`, `crm.customer-events.v1` (twice).

localhost:9092 is the host value from lab 30. Inside the Compose network it is
kafka:9092, so it belongs in application.yml with an env override rather than
hard coded in the class. The topic name is the same string in the publisher and
the listener, and lab 30 froze it as crm.customer-events.v1.

The listener above only names crm-notifications. crm-audit is a second listener
on the same topic with its own group id, and giving both the same group would
make them share the partitions instead of each getting a copy.

## Step 3 — Key reminder

Add a comment: key argument must be `CUS-1001` / `CUS-1002`, not a random UUID.

Comment is on the send call. A UUID key is unique per event, so every event for
Amina would hash to a different partition and there would be no order between
her create and her status change. The key is what ties the events together, not
the customerId inside the JSON.

## Step 4 — DLT blank

Add one line TODO: `// TODO Lab 31: route poison messages to crm.customer-events.v1.dlq`.

Line is at the bottom of the snippet. The topic exists from lab 30 and nothing
writes to it yet.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab31-todos.md`
- [ x ] All _____ replaced
- [ x ] Customer ID key comment present
- [ x ] DLT TODO line present
