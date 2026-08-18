Lab 37 PostgreSQL design for customers and accounts (reflection questions,
checkpoints)

built as examples\lab37-crm, no application code, five numbered SQL
scripts against PostgreSQL 17 in Docker with the named volume
crm_pgdata. the work was the ER model and identifier rules, the crm_app
least-privilege role, DDL for customer, account, address and
customer_status_history with 17 named constraints, three indexes, the
Amina and Ravi fixtures, six negative tests inside savepoints, and a drop
and recreate from empty. followed the GUIDE's full path, not the timed
path, so email_normalized, NUMERIC(19,2) money and the four-value status
CHECK including SUSPENDED.

the GUIDE is a PostgreSQL lab with Oracle text left in it, ORA error
numbers and DROP TABLE CASCADE CONSTRAINTS PURGE. the PostgreSQL
equivalents are used and the mapping is written out in
database\design-decisions.md rather than swapped silently.


REFLECTION QUESTIONS

1. Which design decision most affected correctness?

splitting customer_id from public_id. the surrogate is a BIGINT identity
that joins and foreign keys use, public_id is the immutable CUS-1001 the
API and the UI carry, and the seed looks its parent up by public_id
instead of hardcoding customer_id = 1. that is why drop and recreate
produced identical rows on a table whose identity counter had restarted.
had the seed assumed the surrogate, the whole repeatability proof would
have been a coincidence of running it once.

2. What evidence proves the implementation works?

the negative tests, because green seeds only prove the happy path. six
inserts and deletes each fail with the SQLSTATE and the constraint name
that should have stopped them, 23514 on the status CHECK, 23505 on the
email UNIQUE, 23503 on the orphan account and again on deleting Amina
while she owns one, 23502 on the null name, 23514 again on an uppercase
email. after all of them the counts are still 2 customers, 1 account and 1
history row. beside that the EXPLAIN output naming ix_account_customer and
ix_history_customer_time, and the five privilege probes that crm_app
cannot pass.

3. Which failure was hardest to diagnose?

docker rather than sql. Docker Desktop refused to start, reporting it
could not remove a stale socket at
AppData\Local\Docker\run\userAnalyticsOtlpHttp.sock. the file was zero
bytes, six days old, and a reparse point that could not be opened,
deleted, cleared with fsutil, or reached through the \\?\ path form even
with every docker process stopped. renaming the parent run directory aside
was what worked, docker recreated it and started. inside the SQL the only
surprise was 25P01, savepoints rejected in a psql session that had no
BEGIN, which is a reminder that a savepoint is a marker inside a
transaction and autocommit has none to mark.


CHECKPOINTS

| Checkpoint | Confirm | Result |
| --- | --- | --- |
| A1 | ER cardinalities and identifier decisions documented | Pass, database/er-diagram.md and design-decisions.md |
| A2 | PostgreSQL ready on crm with a volume | Pass, healthy container, volume lab37-crm_crm_pgdata |
| A3 | CRM_APP least-privilege user created | Pass, all role flags false, five escalation probes rejected |
| B1 | Four tables with named constraints | Pass, 17 named constraints listed from pg_constraint |
| B2 | NUMERIC(19,2) money, TIMESTAMPTZ audit columns | Pass, information_schema shows numeric 19,2 |
| B3 | FK indexes created | Pass, three indexes, EXPLAIN shows both in use |
| C1 | Amina with account, Ravi with none | Pass, LEFT JOIN shows Ravi's null account columns |
| C2 | History correlation lab-request-001 | Pass, one history row on Amina's activation |
| C3 | Negative tests recorded, drop and recreate works | Pass, six SQLSTATEs captured, recreate identical |
| D1 | Passwords only in .env, not Git | Pass, .env gitignored, .env.example committed, psql variable for the role password |
| D2 | Design notes and evidence | Pass, design-decisions.md, er-diagram.md, three evidence transcripts |
| D3 | README documents connect and script order | Pass, examples/lab37-crm/README.md |
