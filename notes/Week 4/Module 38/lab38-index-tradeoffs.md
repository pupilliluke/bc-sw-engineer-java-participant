# Lab 38 — Index Tradeoffs

## Step 1 — Benefit

faster status filters and faster account-by-customer joins.

the read win is the one the deck lists on Understanding Indexes, an index
helps WHERE, JOIN, ORDER BY, GROUP BY and DISTINCT, because the database
locates the rows through a sorted structure instead of reading the whole
table. against the patterns in lab38-perf.md that is the status filter on
the customer list and the accounts-for-one-customer join on the detail
screen.

| Index | Pattern it serves | State in my lab 37 schema |
| --- | --- | --- |
| uk_customer_public | open one customer by public_id | exists, unique |
| uk_customer_email | find by email | exists, unique |
| ix_account_customer | accounts by customer_id | exists |
| status / created_at | list filter and date window | nothing yet |

so two thirds of the benefit is already banked from lab 37 step 9. the
part this lab actually has to earn is status and created_at, and those
are the two the exercise is asking me to weigh.

## Step 2 — Cost

slower INSERT and UPDATE for the Amina and Ravi seed rows once the table
is loaded at volume, plus more disk per index.

every index is a second structure the database maintains. an insert
writes the row and then writes an entry into each index on that table, an
update to an indexed column writes the old and new entries, and a delete
removes them. so the write path pays for every index whether or not a
query ever uses it. storage is the other half, each index holds a copy of
the indexed values plus a row pointer.

a unique email index does not help inserts, it adds work to them. the
insert has to check that no other row already holds that email before it
can commit, and that check is the index lookup. `uk_customer_email` is in
my schema because the UNIQUE constraint requires an index to enforce
itself, not because someone picked it for speed. the read speed is a side
effect of the correctness rule.

status alone is the case where the cost buys nothing. the CHECK allows
four values, so `status = 'ACTIVE'` matches a large share of the table,
and reading an index for most of the rows and then fetching each one is
more work than scanning the table once. the planner knows this from the
statistics and picks the sequential scan, and the index sits there being
maintained on every write and read by nothing. it earns its place
composite with created_at, `(status, created_at)`, where the equality
narrows first and the range reads forward.

## Step 3 — Cleanup

the lab drops experimental indexes, so each one I create needs a
before/after recorded while it exists, the plan without it and the plan
with it, for the same query and the same literal. after the drop the
evidence is all that is left. an index kept without that pair is an index
I cannot justify, and one dropped without it is a result I cannot repeat.

## Step 4 — Rule

add an index only when EXPLAIN shows a Seq Scan that a real query pattern
actually needs fixed.

both halves matter. a Seq Scan on a small table is fine and on a rare
query is not worth paying for on every write, so the plan alone is not
the trigger, the plan plus a pattern from lab38-perf.md is.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab38-index-tradeoffs.md`
- [ x ] Benefit stated
- [ x ] Write-cost stated
- [ x ] Measure-first rule
