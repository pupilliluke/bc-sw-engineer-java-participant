# Lab 30 — Fill Kafka Basics TODOs

## Step 1 — Copy the quiz

1. A topic is a named stream of records.
2. A partition is an ordered subset of a topic; offsets are per partition.
3. The offset is the consumer's position in a partition.
4. Consumers in the same consumer group compete for partitions; different groups each get a copy.

## Step 2 — Fill blanks

Replace each `_____` with: topic / partition / offset / consumer group (one each).

topic, partition, offset, consumer group. Offset is not a global message id, it counts up per partition,
and each group tracks its own offsets.

## Step 3 — CRM example

Add one line: group `crm-notifications` shares partitions; group `crm-audit` reads all `CUS-1001`/`CUS-1002` events independently.

Group crm-notifications splits the 3 partitions across its consumers so each event is handled once, while
group crm-audit gets its own copy of every CUS-1001 / CUS-1002 event on the same topic.

## Step 4 — Self-check

Compare to Step 2 answer key in your head: topic, partition, offset, consumer group.

Matches.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab30-kafka-todos.md`
- [ x ] All four terms filled
- [ x ] CRM competing vs independent groups noted
- [ x ] File saved under notes/
