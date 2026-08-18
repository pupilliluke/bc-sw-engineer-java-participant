-- Lab 38 steps 3-5 — statistics, distribution, binds, and the baseline plan.
-- Run as crm_app after 01_generate_data.sql:
--   psql -h localhost -U crm_app -d crm -f database/performance/02_baseline.sql
--
-- The guide's blocks here are Oracle: DBMS_STATS.GATHER_TABLE_STATS, the
-- user_tables view, ALTER SESSION SET statistics_level, VARIABLE/EXEC binds,
-- the gather_plan_statistics hint and DBMS_XPLAN.DISPLAY_CURSOR. None of them
-- exist in PostgreSQL. Originals are at the bottom of this file; the running
-- versions below are the PostgreSQL equivalents the deck and the module 38
-- exercises both specify.

\set ON_ERROR_STOP on
SET search_path = crm_app;

-- Step 3 — statistics.
-- ANALYZE replaces DBMS_STATS. There is no cascade argument, each table is
-- named. Plans read before this are fiction, the planner is still holding row
-- estimates from before the bulk load.
ANALYZE customer;
ANALYZE account;

-- Evidence that the statistics are current. This replaces user_tables.
-- Note: PostgreSQL folds unquoted identifiers to LOWER case, not UPPER as the
-- guide's failure note says. 'CUSTOMER' matches nothing here, 'customer' does.
SELECT s.relname,
       c.reltuples::bigint AS est_rows,
       s.last_analyze,
       s.last_autoanalyze
FROM pg_stat_user_tables s
JOIN pg_class c ON c.oid = s.relid
WHERE s.relname IN ('customer', 'account')
ORDER BY s.relname;

-- Step 4 — distribution and binds.
SELECT COUNT(*) AS cnt, status FROM customer GROUP BY status ORDER BY status;

SELECT COUNT(*) AS customers_with_accounts
FROM customer c WHERE EXISTS (SELECT 1 FROM account a WHERE a.customer_id = c.customer_id);

-- Binds measured throughout this lab. Fixed values, not whatever the last run
-- happened to use, or the before and after plans are not comparable.
--   email bind:     user000001@example.test   (a bulk row, typical selectivity)
--   email bind alt: amina@example.com         (fixture, also unique)
--   public_id bind: CUS-1001
--   status bind:    ACTIVE
--   day bind:       2026-07-01                (inside the generated 90-day spread)
\set email 'user000001@example.test'

-- Step 5 — baseline plan for the email lookup.
-- EXPLAIN alone gives the planner's estimate. ANALYZE runs the statement and
-- adds actual rows and actual time, BUFFERS adds the blocks read. Estimated
-- against actual rows is the number that says whether the statistics are stale.
EXPLAIN (ANALYZE, BUFFERS)
SELECT customer_id, public_id, full_name, status
FROM customer
WHERE email_normalized = :'email';

-- Expected here is NOT the guide's TABLE ACCESS FULL. The lab 37 DDL this lab
-- inherits already declares CONSTRAINT uk_customer_email UNIQUE
-- (email_normalized), and a unique constraint is backed by an index, so the
-- baseline is already an Index Scan on uk_customer_email. Recorded as
-- lab38-001 with that stated, because step 6's CREATE UNIQUE INDEX is then a
-- second index on the same column and has nothing left to improve.

-- ---------------------------------------------------------------------------
-- Guide versions, Oracle only, do not run on PostgreSQL. Kept for the diff.
--
-- Step 3:
-- EXEC DBMS_STATS.GATHER_TABLE_STATS(USER, 'CUSTOMER', cascade => TRUE);
-- EXEC DBMS_STATS.GATHER_TABLE_STATS(USER, 'ACCOUNT', cascade => TRUE);
-- SELECT table_name, num_rows, last_analyzed
-- FROM user_tables
-- WHERE table_name IN ('CUSTOMER', 'ACCOUNT');
--
-- Step 5:
-- ALTER SESSION SET statistics_level = ALL;
-- VARIABLE email VARCHAR(320)
-- EXEC :email := 'user000001@example.test';
-- SELECT /*+ gather_plan_statistics */ customer_id, public_id, full_name, status
-- FROM customer
-- WHERE email_normalized = :email;
-- SELECT * FROM TABLE(
--   DBMS_XPLAN.DISPLAY_CURSOR(NULL, NULL, 'ALLSTATS LAST +PREDICATE')
-- );
