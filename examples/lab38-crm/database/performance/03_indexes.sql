-- Lab 38 step 6 — index the selective email lookup, with a measured
-- before and after. Run as crm_app after 02_baseline.sql:
--   psql -h localhost -U crm_app -d crm -f database/performance/03_indexes.sql
--
-- The guide's step 6 is one CREATE UNIQUE INDEX on customer (email_normalized)
-- followed by a re-run of the step 5 query, expecting the plan to move off a
-- full scan. That cannot happen against this schema: the Lab 37 DDL already
-- declares CONSTRAINT uk_customer_email UNIQUE (email_normalized) and
-- PostgreSQL backs a unique constraint with a unique index, so the column was
-- indexed before the lab started and the step 5 baseline was already an Index
-- Scan. Creating ux_customer_email_norm on top would be a second B-tree over
-- the same 50,002 values and would measure nothing.
--
-- This script instead removes the inherited index, measures the genuinely
-- unindexed state, rebuilds the index, measures again, and then restores the
-- Lab 37 constraint by name. The "before" is therefore manufactured on
-- purpose, which is recorded in report.md rather than presented as the
-- schema's natural starting point.
--
-- While the constraint is dropped, nothing prevents duplicate emails. This
-- container has no other writer, and the constraint is back by the end of the
-- script, but the window is real and is noted in the report.

\set ON_ERROR_STOP on
SET search_path = crm_app;

\set email 'user000001@example.test'

\echo '=== 1. write cost WITH the unique index (rolled back) ==='
BEGIN;
EXPLAIN (ANALYZE, BUFFERS)
INSERT INTO customer (public_id, full_name, email_normalized, status)
SELECT 'CUS-WTEST-' || lpad(i::text, 6, '0'),
       'Write Test ' || lpad(i::text, 6, '0'),
       'wtest' || lpad(i::text, 6, '0') || '@example.test',
       'PROSPECT'
FROM generate_series(1, 5000) AS i;
ROLLBACK;

\echo '=== 2. drop the inherited unique constraint ==='
ALTER TABLE customer DROP CONSTRAINT uk_customer_email;
ANALYZE customer;

\echo '=== 3. read plan with NO index on email_normalized ==='
EXPLAIN (ANALYZE, BUFFERS)
SELECT customer_id, public_id, full_name, status
FROM customer
WHERE email_normalized = :'email';

\echo '=== 4. write cost WITHOUT the unique index (rolled back) ==='
BEGIN;
EXPLAIN (ANALYZE, BUFFERS)
INSERT INTO customer (public_id, full_name, email_normalized, status)
SELECT 'CUS-WTEST-' || lpad(i::text, 6, '0'),
       'Write Test ' || lpad(i::text, 6, '0'),
       'wtest' || lpad(i::text, 6, '0') || '@example.test',
       'PROSPECT'
FROM generate_series(1, 5000) AS i;
ROLLBACK;

\echo '=== 5. create the index the guide asks for ==='
CREATE UNIQUE INDEX ux_customer_email_norm ON customer (email_normalized);
ANALYZE customer;

\echo '=== 6. read plan WITH the index, identical query and bind ==='
EXPLAIN (ANALYZE, BUFFERS)
SELECT customer_id, public_id, full_name, status
FROM customer
WHERE email_normalized = :'email';

\echo '=== 7. restore the Lab 37 contract ==='
-- Promotes the index into the named constraint rather than leaving a second
-- structure behind. The index is renamed to uk_customer_email by this, so the
-- schema ends exactly where Lab 37 left it and Lab 39 inherits the same names.
ALTER TABLE customer ADD CONSTRAINT uk_customer_email UNIQUE USING INDEX ux_customer_email_norm;

\echo '=== 8. final index state on customer ==='
SELECT indexname, indexdef FROM pg_indexes
WHERE schemaname = 'crm_app' AND tablename = 'customer'
ORDER BY indexname;

SELECT COUNT(*) AS customers FROM customer;

-- ---------------------------------------------------------------------------
-- Lab 38 step 7 — active-list index.
-- The CRM list screen filters status equality and orders newest first with a
-- deterministic tiebreak. The guide's measurement uses the
-- gather_plan_statistics hint and DBMS_XPLAN; same swap as above, EXPLAIN
-- (ANALYZE, BUFFERS). FETCH FIRST is standard SQL and runs on PostgreSQL as
-- written. Three runs each side so the report can quote a median.

\echo '=== 9. drop step-7 index if rerunning ==='
DROP INDEX IF EXISTS ix_customer_status_created;
ANALYZE customer;

\echo '=== 10. ACTIVE list plan BEFORE index (x3) ==='
EXPLAIN (ANALYZE, BUFFERS)
SELECT customer_id, public_id, created_at
FROM customer
WHERE status = 'ACTIVE'
ORDER BY created_at DESC, customer_id DESC
FETCH FIRST 20 ROWS ONLY;
EXPLAIN (ANALYZE, BUFFERS)
SELECT customer_id, public_id, created_at
FROM customer
WHERE status = 'ACTIVE'
ORDER BY created_at DESC, customer_id DESC
FETCH FIRST 20 ROWS ONLY;
EXPLAIN (ANALYZE, BUFFERS)
SELECT customer_id, public_id, created_at
FROM customer
WHERE status = 'ACTIVE'
ORDER BY created_at DESC, customer_id DESC
FETCH FIRST 20 ROWS ONLY;

\echo '=== 11. create the active-list index ==='
CREATE INDEX ix_customer_status_created
  ON customer (status, created_at DESC, customer_id DESC);
ANALYZE customer;

\echo '=== 12. ACTIVE list plan AFTER index, identical query (x3) ==='
EXPLAIN (ANALYZE, BUFFERS)
SELECT customer_id, public_id, created_at
FROM customer
WHERE status = 'ACTIVE'
ORDER BY created_at DESC, customer_id DESC
FETCH FIRST 20 ROWS ONLY;
EXPLAIN (ANALYZE, BUFFERS)
SELECT customer_id, public_id, created_at
FROM customer
WHERE status = 'ACTIVE'
ORDER BY created_at DESC, customer_id DESC
FETCH FIRST 20 ROWS ONLY;
EXPLAIN (ANALYZE, BUFFERS)
SELECT customer_id, public_id, created_at
FROM customer
WHERE status = 'ACTIVE'
ORDER BY created_at DESC, customer_id DESC
FETCH FIRST 20 ROWS ONLY;

\echo '=== 13. index state after step 7 ==='
SELECT indexname FROM pg_indexes
WHERE schemaname = 'crm_app' AND tablename = 'customer'
ORDER BY indexname;
