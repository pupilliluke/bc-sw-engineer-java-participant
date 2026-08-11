Northstar CRM events (Lab 30)

  docker compose up -d
  docker compose ps

  docker exec crm-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic crm.customer-events.v1 --partitions 3 --replication-factor 1
  docker exec crm-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic crm.customer-events.v1.dlq --partitions 1 --replication-factor 1
  docker exec crm-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic crm.customer-events.v1
  # expect PartitionCount: 3

  docker exec -it crm-kafka /opt/kafka/bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic crm.customer-events.v1 --property parse.key=true --property key.separator=:
  # CUS-1001:{...}  key before the first colon, Ctrl+C to exit, do not press
  # Enter on an empty line

  docker exec crm-kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic crm.customer-events.v1 --from-beginning --property print.key=true --property print.partition=true --property print.offset=true --property print.timestamp=true --max-messages 3
  # expect CUS-1001 twice on one partition with increasing offsets

  mvn -B -q -DskipTests package
  mvn -B exec:java "-Dexec.mainClass=com.northstar.crm.event.CustomerEventProducer"
  # expect topic=crm.customer-events.v1 partition=0 offset=3

  docker exec crm-kafka /opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group crm-notifications

  git status --short

Copied from the lab 30 starter. The starter ships compose.yaml, the three event
JSON samples and CustomerEventProducer with the acks, idempotence and send
TODOs. Raw kafka-clients only, no Spring Kafka until lab 31.

The -D argument needs the quotes on PowerShell. Without them PowerShell splits
at the dot and Maven reads .mainClass=... as a lifecycle phase. Run mvn from
this directory, the producer reads events/customer-created-amina.json as a
relative path.

CLEANUP

  docker compose down
  mvn -q clean
  git status --short

docker compose down -v deletes the topics and offsets, so only with intent.
target/ and .env are ignored. Keep lab30-crm and the topic names, lab 31 wires
Spring Kafka consumers onto crm.customer-events.v1 and the DLQ.

NOTES

Topic, key and envelope decisions are in docs/kafka-notes.md with the runbook a
peer needs. Evidence and the failure experiments are in
java-bootcamp/notes/screenshots/lab-30/. Checkpoints and reflection answers are in
notes/Week 4/Module 30/lab30-answers.md. Full GUIDE at
labs/Week 4 - Kafka, React, PostgreSQL and Resilience/module-30/lab30/.
