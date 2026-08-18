# Lab 37 — Seed and Verify Plan

## Step 1 — Seed order

customers before accounts. the FK is checked at insert time, so an account
row naming CUS-1001 fails with 23503 until that customer exists. the same
ordering runs in reverse on the way out, delete accounts before customers,
which is the RESTRICT policy from the ER sketch doing its job.

## Step 2 — Verify SQL

```sql
SELECT customer_id, full_name FROM customer ORDER BY customer_id;
```

expected: two rows, CUS-1001 Amina Khan and CUS-1002 Ravi Singh, in that
order because the ids sort that way.

## Step 3 — Join check

```sql
SELECT a.account_number, a.account_type
FROM account a
JOIN customer c ON a.customer_id = c.customer_id
WHERE c.customer_id = 'CUS-1001';
```

Amina's accounts by customer_id. the same query with CUS-1002 returns
zero rows, and that is the point of Ravi as a fixture, an inner join drops
the parent when there is no child. a LEFT JOIN from customer would keep
Ravi with nulls in the account columns, which is the version to use when
the question is "every customer and their accounts" rather than "the
accounts of this customer".

## Step 4 — No execute

nothing here runs in the pre-lab. no psql, no docker compose up, no
connection string. the lab executes it against a real instance with a
CRM_APP user, this file is the plan it follows.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab37-seed-and-verify-plan.md`
- [ x ] Insert order correct
- [ x ] Verify SELECT written
- [ x ] No-execute confirmation
