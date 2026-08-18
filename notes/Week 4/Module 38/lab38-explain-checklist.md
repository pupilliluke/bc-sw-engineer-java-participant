# Lab 38 — EXPLAIN Checklist

## Step 1 — Command

```sql
EXPLAIN (ANALYZE, BUFFERS) SELECT customer_id, full_name, status
FROM customer WHERE customer_id = 'CUS-1001';
```

plain EXPLAIN gives the plan the optimizer chose and its estimates
without running the query. ANALYZE runs it and adds the actual time and
the actual row counts, BUFFERS adds how many blocks were read and whether
they came from cache. the estimate on its own is the thing the deck warns
against reading forever, the gap between estimated and actual rows is
what tells me the statistics are stale.

## Step 2 — Look for

Seq Scan vs Index Scan, the estimated rows, and the buffers line.

| Line | Meaning | What it tells me |
| --- | --- | --- |
| Seq Scan | reads the table row by row, no index helped | the candidate to fix, if a real pattern needs it |
| Index Scan | found the rows through an index | the index is being used |
| cost=a..b | estimated startup..total | comparable between two plans for the same query |
| rows / width | estimated rows returned and row size | compare against actual rows |
| actual time / loops | real time and how many times it ran | only present with ANALYZE |
| Buffers: shared hit/read | blocks from cache vs from disk | the work the timing alone hides |

rows examined against rows returned is the ratio that names the problem.
a plan that reads the whole customer table to return one row is doing the
work the index was supposed to remove.

## Step 3 — Success signal

an Index Scan on the customer lookup for Amina is the good sign, one row
estimated, one row actual, buffers in single digits.

the deck writes that lookup as `customer_id = 'CUS-1001'`. in my lab 37
schema `CUS-1001` is `public_id` and customer_id is the BIGINT identity,
so the plan line I expect to see names `uk_customer_public`. same signal
either way, an index scan rather than a sequential one.

## Step 4 — Analyze

`ANALYZE customer;` refreshes the planner statistics in PostgreSQL. this
is not DBMS_STATS, that is the Oracle name and it does not exist here.

stale statistics are the answer to a plan that looks wrong for no
reason, the optimizer is choosing against an index because its row
estimate is from before the bulk load. so if a Seq Scan shows up on
email equality where a unique index exists, the first check is the
estimated rows against the actual rows, then ANALYZE, then re-run the
plan before touching any index.

fixed literals in every run. the same value each time or the plans are
not comparable, a lookup for a customer with one account and a lookup
for one with forty are different queries wearing the same SQL.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab38-explain-checklist.md`
- [ x ] EXPLAIN command written
- [ x ] Scan types named
- [ x ] ANALYZE noted
