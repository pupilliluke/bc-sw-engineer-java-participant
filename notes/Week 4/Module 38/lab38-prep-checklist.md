# Lab 38 prep checklist

## Earlier exercise files present?

| File | Present? (yes/no) |
| ---- | ----------------- |
| notes/lab38-perf.md | yes |
| notes/lab38-index-tradeoffs.md | yes |
| notes/lab38-sql-index-todos.md | yes |
| notes/lab38-explain-checklist.md | yes |
| notes/lab38-sargability.md | yes |

kept under notes/Week 4/Module 38/ with the rest of the module notes.
`lab38-todos.sql` sits beside them, it is the filled starter from
exercise 3 rather than a sixth note.

## Schema readiness

lab 37 finished, so `customer` and `account` exist with the columns this
lab tunes: public_id, email_normalized, status and created_at on
customer, customer_id on account. the indexes already in place from step
9 are `uk_customer_public`, `uk_customer_email` and `ix_account_customer`,
so the baseline is not an unindexed table and the before plans have to be
read with that in mind. status and created_at carry nothing, which is
where the lab's own experiments land.

## Fixtures (verify)

| ID | Name | Status |
| -- | ---- | ------ |
| CUS-1001 | Amina Khan | ACTIVE |
| CUS-1002 | Ravi Singh | PROSPECT |

Amina owns an account, Ravi owns none. correlation `lab-request-001`.
the bulk load in this lab generates volume around these two, so the check
after loading is that both are still there and still hold those statuses,
a generator that truncates first would take them with it.

## Hypothesis

before any index, the status filter and the created_at window both come
back Seq Scan, and the single-customer lookup is already an Index Scan
because the unique index exists from lab 37.

after `(status, created_at)` the date-window-within-a-status query turns
into an Index Scan, and status alone stays Seq Scan even with the index
present because four values across the table is not selective enough for
the planner to prefer it. the account join is unchanged either way, it
already has its index.

the rewrite of `date_trunc('day', created_at) = ...` into the half-open
range flips that query from Seq Scan to Index Scan with no new index at
all, so it is the cheapest win in the lab and the one to measure first.

## PostgreSQL tools

`EXPLAIN (ANALYZE, BUFFERS) <sql>;` for the plans, `ANALYZE customer;`
for statistics, `\di` for what indexes exist, `pg_stat_statements` for
slow queries by total duration. no DBMS_XPLAN and no DBMS_STATS, those
are the Oracle names from older materials.

## Runtime

Docker 27.3.1 and psql 17.6 on this machine, same as lab 37, so the lab
runs against a local container. no shared or production instance, the
whole point of the lab is creating and dropping indexes and that is not
something to do on a database anyone else is using. nothing started in
the pre-lab.

## Measure-before-index

no index goes in without a before plan. a plan can be captured after the
fact but not the plan that justified the change, and an index kept
without that pair is one I cannot defend when the lab asks why it stayed.

lab 39 maps this schema with JPA, so the indexes kept here are the ones
those repositories inherit. that is the reason to drop the experiments
that did not earn their place rather than leave them.

## Scope statement

Pre-lab only — prepare for lab; do not complete full Lab 38 now.

## Self mark

Overall prep: Pass
If Fail, revisit exercise(s): n/a
