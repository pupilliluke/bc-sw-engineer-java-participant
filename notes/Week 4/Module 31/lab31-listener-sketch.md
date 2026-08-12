# Lab 31 — Listener Sketch

## Step 1 — Method outline

`@KafkaListener(topics="crm.customer-events.v1", groupId="crm-notifications")` void onCustomerEvent(...).

    @KafkaListener(topics = "crm.customer-events.v1", groupId = "crm-notifications")
    void onCustomerEvent(@Payload CustomerEvent event,
                         @Header(KafkaHeaders.RECEIVED_KEY) String key,
                         @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                         @Header(KafkaHeaders.OFFSET) long offset)

The key comes in as a header rather than as part of the payload, and it is the
only place the partitioning decision is visible to the consumer. Reject or route
to the DLT when key does not equal event.customerId. Lab 30 put a record on the
topic keyed CUS-1001 whose payload said CUS-1002 and the broker accepted it, so
the check is for a record that already exists on this topic.

## Step 2 — Second group

Sketch the audit listener with groupId `crm-audit` on the same topic.

    @KafkaListener(topics = "crm.customer-events.v1", groupId = "crm-audit")
    void onCustomerEventForAudit(@Payload CustomerEvent event,
                                 @Header(KafkaHeaders.RECEIVED_KEY) String key)

Same topic, different group id, so it gets its own copy of every record and its
own offsets. Lab 30 showed this with the CLI, crm-notifications members split
the three partitions and crm-audit read all 12 records the other group had
already consumed. The two group ids have to stay distinct, sharing one would
make the audit listener take partitions off the notification listener instead
of reading independently.

## Step 3 — Payload type

Decide: start with `String`/`JsonNode` or a typed `CustomerEvent` DTO — pick one and justify in one line.

Typed CustomerEvent DTO for the envelope with data left as a Map, because
eventId, eventType, eventVersion, customerId and correlationId are identical on
every event and worth compile-time safety, while the two payload shapes are not.

crm.customer-events.v1 carries CustomerCreated with fullName and status and
CustomerStatusChanged with oldStatus and newStatus, so one typed data field
cannot hold both. Leaving data as a Map also keeps the forward-compatible rule
from the lab 30 envelope sketch, a producer adding a field does not break the
listener.

## Step 4 — Correlation

Note where you will log `correlationId` / `lab-request-001` for support.

Log it on entry to each listener, before any payload handling, together with the
topic, partition, offset and the record key. correlationId is an envelope field
so it is readable even when data is the part that is malformed. Log it again in
the error path and on anything sent to the DLT, otherwise the records support is
asking about are the only ones with no line in the log.

It is the same id lab 29 carries as X-Correlation-Id and echoes in the error
envelope, defaulting to lab-request-001. The HTTP thread is gone by the time the
notifications listener runs, so the envelope is what keeps one id across the
async boundary and lets support follow lab-request-001 from the POST that
created Amina to the notification consumer that handled it.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab31-listener-sketch.md`
- [ x ] Both listeners sketched with distinct group ids
- [ x ] Payload type chosen and justified
- [ x ] Correlation logging point noted
