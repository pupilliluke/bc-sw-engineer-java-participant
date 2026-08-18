# Lab 37 — Constraints Checklist

## Step 1 — PK/UK

PK on customer_id; UNIQUE on account_number.

| Constraint | Column | Why |
| --- | --- | --- |
| PRIMARY KEY | customer.customer_id | one row per customer, the FK target |
| UNIQUE | account.account_number | two accounts must not share a number |
| PRIMARY KEY | account.account_id | surrogate, stable while the number could be reissued |
| FOREIGN KEY | account.customer_id | every account points at a real customer |

the primary key is the identifier the rest of the schema hangs off, and
the unique constraint is a rule about a value people quote to each other.
they are not interchangeable, a table gets one PK and as many UNIQUE
constraints as it has such values.

## Step 2 — CHECK

status IN ('PROSPECT','ACTIVE','SUSPENDED','CLOSED'). the four the deck
lists. the UI has only carried PROSPECT, ACTIVE and CLOSED since lab 34,
SUSPENDED is in the database vocabulary and not yet in the form, which is
fine in that direction, a value the app never sends is harmless while a
value the app sends and the CHECK rejects is a runtime error.

named `ck_customer_status`, not left to the database to name. a named
constraint is what shows up in the error message, so a failing insert says
which rule it broke.

## Step 3 — NOT NULL

full_name and status NOT NULL, plus customer_id, created_at, and on
account every column except nothing, account_id, customer_id,
account_number and account_type are all mandatory. optional columns are
the ones a real customer record can genuinely be missing on day one, an
address line or a phone, and there are none of those in this pair of
tables.

## Step 4 — SQLSTATE awareness

a unique violation raises SQLSTATE 23505, and the lab's negative tests
trigger it on purpose by inserting a duplicate account_number. the
neighbours worth knowing: 23503 foreign key violation, 23502 not null
violation, 23514 check violation. these are the codes an application
catches rather than parsing message text, which is how a duplicate becomes
a 409 instead of a 500.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab37-constraints.md`
- [ x ] PK/UK listed
- [ x ] CHECK drafted
- [ x ] 23505 noted
