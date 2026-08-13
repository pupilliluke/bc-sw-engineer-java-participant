Northstar CRM Spring Kafka (Lab 31)

  mvn -B test
  # expect Tests run: 4, Failures: 0, Errors: 0

The suite runs on EmbeddedKafka and needs no broker. Everything below needs the
lab 30 broker.

  docker compose -f ../lab30-crm/compose.yaml up -d
  docker compose -f ../lab30-crm/compose.yaml ps

  docker exec crm-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic crm.customer-events.v1
  # expect PartitionCount: 3

  mvn -B spring-boot:run
  # watch for customer_event_published, the listener correlation line, and
  # duplicate_event_ignored on a replay

  docker exec crm-kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic crm.customer-events.v1.dlq --from-beginning --property print.headers=true

  docker exec crm-kafka /opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group crm-notifications

  git status --short

Copied from the lab 31 starter. The starter ships the Boot baseline,
CustomerEvent, the publisher, the listener, ProcessedEventStore, KafkaErrorConfig
and an EmbeddedKafka test with the bodies as TODOs. Timed path, so there is no
web or service layer, the publisher is called from the tests.

Topics come from lab 30 and this lab does not create them. Dead letters go to
crm.customer-events.v1.dlq, not Spring's default of crm.customer-events.v1.DLT.

CLEANUP

  mvn -q clean
  docker compose -f ../lab30-crm/compose.yaml down
  git status --short

docker compose down -v deletes the topics and offsets, so only with intent.
target/ and .env are ignored. Keep lab31-crm, lab 32 adds Resilience4j on top of
this listener.

NOTES

Publish path, idempotency, DLT naming and the runbook are in
docs/spring-kafka-notes.md. Evidence and the failure experiments are in
java-bootcamp/notes/screenshots/lab-31/. Checkpoints and reflection answers are
in notes/Week 4/Module 31/lab31-answers.md. Full GUIDE at
labs/Week 4 - Kafka, React, PostgreSQL and Resilience/module-31/lab31/.
