-- Lab 37 step 11 — verification. Run as crm_app after 03_seed.sql:
--   psql -f database/04_verify.sql
-- Positive checks first, then negative tests inside savepoints. ON_ERROR_STOP
-- stays OFF here on purpose: the negative inserts are supposed to fail, and
-- each ROLLBACK TO SAVEPOINT brings the aborted transaction back so the next
-- one can run. The script ends with no net change.

\set ON_ERROR_STOP off
SET search_path = crm_app;

\echo '== positive: customers =='
SELECT public_id, full_name, status FROM customer ORDER BY public_id;

\echo '== positive: accounts per customer, Ravi must show NULL =='
SELECT c.public_id, a.account_number, a.balance, a.currency
FROM customer c
LEFT JOIN account a ON a.customer_id = c.customer_id
ORDER BY c.public_id;

\echo '== positive: history correlation =='
SELECT c.public_id, h.old_status, h.new_status, h.correlation_id
FROM customer_status_history h
JOIN customer c ON c.customer_id = h.customer_id
ORDER BY h.changed_at;

\echo '== positive: money type is exact, not binary float =='
SELECT column_name, data_type, numeric_precision, numeric_scale
FROM information_schema.columns
WHERE table_schema = 'crm_app' AND table_name = 'account' AND column_name = 'balance';

\echo '== positive: named constraints =='
SELECT conname, contype
FROM pg_constraint
WHERE connamespace = 'crm_app'::regnamespace
ORDER BY conname;

BEGIN;

\echo '== negative 1: invalid status, expect SQLSTATE 23514 check_violation =='
SAVEPOINT negative_test;
INSERT INTO customer (public_id, full_name, email_normalized, status)
VALUES ('CUS-X', 'Bad Status', 'bad@example.com', 'UNKNOWN');
ROLLBACK TO SAVEPOINT negative_test;

\echo '== negative 2: duplicate email, expect SQLSTATE 23505 unique_violation =='
SAVEPOINT negative_test;
INSERT INTO customer (public_id, full_name, email_normalized, status)
VALUES ('CUS-DUPE', 'Dupe', 'amina@example.com', 'PROSPECT');
ROLLBACK TO SAVEPOINT negative_test;

\echo '== negative 3: orphan account, expect SQLSTATE 23503 foreign_key_violation =='
SAVEPOINT negative_test;
INSERT INTO account (account_number, customer_id, account_type, balance)
VALUES ('ACCT-ORPHAN', 999999, 'CHECKING', 0);
ROLLBACK TO SAVEPOINT negative_test;

\echo '== negative 4: missing full_name, expect SQLSTATE 23502 not_null_violation =='
SAVEPOINT negative_test;
INSERT INTO customer (public_id, full_name, email_normalized, status)
VALUES ('CUS-NULL', NULL, 'null@example.com', 'PROSPECT');
ROLLBACK TO SAVEPOINT negative_test;

\echo '== negative 5: delete a customer that owns an account, expect 23503 from ON DELETE RESTRICT =='
SAVEPOINT negative_test;
DELETE FROM customer WHERE public_id = 'CUS-1001';
ROLLBACK TO SAVEPOINT negative_test;

COMMIT;

\echo '== after negatives: seeds intact, still exactly two customers =='
SELECT count(*) AS customers FROM customer;
SELECT count(*) AS accounts FROM account;
SELECT count(*) AS history_rows FROM customer_status_history;
