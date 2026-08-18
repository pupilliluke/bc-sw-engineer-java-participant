# Lab 37 — ER Sketch

## Reference

| Relationship | Cardinality |
| --- | --- |
| customer → account | 1:N |
| account.customer_id | FK → customer.customer_id |
| customer.customer_id | PK / unique business key |

N includes zero. Ravi is the seed that proves it, a customer with no
account row at all.

## Step 2 — Diagram

```
erDiagram
    CUSTOMER ||--o{ ACCOUNT : owns
    CUSTOMER {
        string customer_id PK
        string full_name
        string status
        timestamptz created_at
    }
    ACCOUNT {
        bigint account_id PK
        string customer_id FK
        string account_number UK
        string account_type
    }
```

the crow's foot with the o is zero-or-more on the account side, and the
double bar on the customer side is exactly one, an account cannot exist
without its customer.

## Step 3 — Cascade policy

ON DELETE RESTRICT on account.customer_id. an account is a financial
record and deleting the customer row should not quietly take the account
rows with it, the delete should fail and make someone decide what happens
to the accounts first. CASCADE is the right answer for rows that only
exist to describe their parent, an address or a status history line, not
for something the business keeps its own books against. this is also
PostgreSQL's default behaviour when no action is named, so writing
RESTRICT states the intent rather than changing it.

## Step 4 — Boundary

no kafka outbox table here. the outbox belongs to the event work in
modules 30 and 31, and nothing in this module's design needs it.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab37-er-sketch.md`
- [ x ] 1:N stated
- [ x ] Diagram present
- [ x ] Delete policy justified
