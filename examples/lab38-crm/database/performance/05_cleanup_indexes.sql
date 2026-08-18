-- Lab 38 step 12 — challenge cycle. Run as crm_app:
--   psql -h localhost -U crm_app -d crm -f database/performance/05_cleanup_indexes.sql
--
-- Every index this lab could drop is challenged with a measured regression
-- and recreated only because the evidence justifies it. The constraint-backed
-- indexes (pk_customer, uk_customer_public, uk_customer_email,
-- uk_account_number) are not challenged by DDL here: dropping them removes
-- the constraint, the Lab 37 verify script asserts those constraints by name,
-- and Lab 39 inherits them as its contract. uk_customer_email's regression
-- was measured under controlled conditions in 03_indexes.sql instead.
-- ix_account_customer's value is evidenced by the step 9 nested-loop plan.

\set ON_ERROR_STOP on
SET search_path = crm_app;

\echo '=== challenge ix_customer_status_created ==='

\echo '--- 1. with index: ACTIVE list first page ---'
EXPLAIN (ANALYZE, BUFFERS)
SELECT customer_id, public_id, created_at
FROM customer
WHERE status = 'ACTIVE'
ORDER BY created_at DESC, customer_id DESC
FETCH FIRST 20 ROWS ONLY;

\echo '--- 2. with index, status equality alone, no ORDER BY: prediction is'
\echo '--- the planner ignores the index, ACTIVE is 70% of the table ---'
EXPLAIN (ANALYZE, BUFFERS)
SELECT COUNT(*) FROM customer WHERE status = 'ACTIVE';

\echo '--- 3. drop ---'
DROP INDEX ix_customer_status_created;
ANALYZE customer;

\echo '--- 4. without index: ACTIVE list first page, the regression ---'
EXPLAIN (ANALYZE, BUFFERS)
SELECT customer_id, public_id, created_at
FROM customer
WHERE status = 'ACTIVE'
ORDER BY created_at DESC, customer_id DESC
FETCH FIRST 20 ROWS ONLY;

\echo '--- 5. without index: write cost of 5,000 inserts, rolled back ---'
BEGIN;
EXPLAIN (ANALYZE, BUFFERS)
INSERT INTO customer (public_id, full_name, email_normalized, status)
SELECT 'CUS-WTEST-' || lpad(i::text, 6, '0'),
       'Write Test ' || lpad(i::text, 6, '0'),
       'wtest' || lpad(i::text, 6, '0') || '@example.test',
       'PROSPECT'
FROM generate_series(1, 5000) AS i;
ROLLBACK;

\echo '--- 6. recreate: the regression justifies it ---'
CREATE INDEX ix_customer_status_created
  ON customer (status, created_at DESC, customer_id DESC);
ANALYZE customer;

\echo '--- 7. restored: ACTIVE list first page ---'
EXPLAIN (ANALYZE, BUFFERS)
SELECT customer_id, public_id, created_at
FROM customer
WHERE status = 'ACTIVE'
ORDER BY created_at DESC, customer_id DESC
FETCH FIRST 20 ROWS ONLY;

\echo '--- 8. with index: write cost of the same 5,000 inserts, rolled back ---'
BEGIN;
EXPLAIN (ANALYZE, BUFFERS)
INSERT INTO customer (public_id, full_name, email_normalized, status)
SELECT 'CUS-WTEST-' || lpad(i::text, 6, '0'),
       'Write Test ' || lpad(i::text, 6, '0'),
       'wtest' || lpad(i::text, 6, '0') || '@example.test',
       'PROSPECT'
FROM generate_series(1, 5000) AS i;
ROLLBACK;

\echo '=== final state: indexes, counts, fixtures preserved ==='
SELECT indexname FROM pg_indexes
WHERE schemaname = 'crm_app' AND tablename IN ('customer', 'account')
ORDER BY indexname;
SELECT COUNT(*) AS customers FROM customer;
SELECT public_id, full_name, status FROM customer
WHERE public_id IN ('CUS-1001', 'CUS-1002') ORDER BY public_id;
