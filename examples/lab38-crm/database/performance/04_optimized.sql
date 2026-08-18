-- TODO: replace DATE_TRUNC/TRUNC(created_at) filters with half-open tstz range
-- Lab 38 step 8 — sargable date rewrite.
-- Guide's contrast query uses Oracle TRUNC(created_at); PostgreSQL is
-- date_trunc('day', created_at). Same wrap, same problem, different name.

\set ON_ERROR_STOP on
SET search_path = crm_app;

\echo '=== non-sargable: function wraps the column ==='
EXPLAIN (ANALYZE, BUFFERS)
SELECT customer_id FROM customer
WHERE date_trunc('day', created_at) = DATE '2026-07-01';

\echo '=== sargable: half-open range on the bare column ==='
EXPLAIN (ANALYZE, BUFFERS)
SELECT customer_id FROM customer
WHERE created_at >= TIMESTAMP '2026-07-01 00:00:00'
  AND created_at <  TIMESTAMP '2026-07-02 00:00:00';

\echo '=== identity proof: both directions must return 0 rows ==='
SELECT customer_id FROM customer
WHERE date_trunc('day', created_at) = DATE '2026-07-01'
EXCEPT
SELECT customer_id FROM customer
WHERE created_at >= TIMESTAMP '2026-07-01 00:00:00'
  AND created_at <  TIMESTAMP '2026-07-02 00:00:00';

SELECT customer_id FROM customer
WHERE created_at >= TIMESTAMP '2026-07-01 00:00:00'
  AND created_at <  TIMESTAMP '2026-07-02 00:00:00'
EXCEPT
SELECT customer_id FROM customer
WHERE date_trunc('day', created_at) = DATE '2026-07-01';

\echo '=== row count for the report ==='
SELECT COUNT(*) AS day_rows FROM customer
WHERE created_at >= TIMESTAMP '2026-07-01 00:00:00'
  AND created_at <  TIMESTAMP '2026-07-02 00:00:00';

-- Guide original, Oracle only, kept for the diff:
-- SELECT customer_id FROM customer
-- WHERE TRUNC(created_at) = DATE '2026-07-01';
-- TODO: keyset page: WHERE (created_at, customer_id) < ($ts, $id) ORDER BY ... LIMIT 50
-- TODO: compare nested loop vs hash join hints/plans for customer→account
-- Lab 38 step 9 — join strategies, customer → account.
-- ix_account_customer already exists from Lab 37 step 9; the guide's CREATE
-- INDEX here would error on the duplicate, so it is measured, not created.

\echo '=== selective join: one customer, Amina ==='
EXPLAIN (ANALYZE, BUFFERS)
SELECT c.public_id, a.account_id, a.balance
FROM customer c
JOIN account a ON a.customer_id = c.customer_id
WHERE c.public_id = 'CUS-1001';

\echo '=== broad join: report over all ACTIVE customers with accounts ==='
EXPLAIN (ANALYZE, BUFFERS)
SELECT c.status, COUNT(*) AS accounts, SUM(a.balance) AS total_balance
FROM customer c
JOIN account a ON a.customer_id = c.customer_id
WHERE c.status = 'ACTIVE'
GROUP BY c.status;

--
-- Lab 38 step 10 — deterministic offset paging.
-- Lab 38 step 10 — deterministic offset paging.
-- Guide's query as written, binds set via psql. page_size 20 to match the
-- step 7 baseline; created_at alone allows ties, customer_id breaks them.

\set page_size 20

\echo '=== page 0 ==='
\set offset 0
EXPLAIN (ANALYZE, BUFFERS)
SELECT customer_id, public_id, created_at
FROM customer
WHERE status = 'ACTIVE'
ORDER BY created_at DESC, customer_id DESC
OFFSET :offset ROWS FETCH NEXT :page_size ROWS ONLY;

\echo '=== page 1 ==='
\set offset 20
EXPLAIN (ANALYZE, BUFFERS)
SELECT customer_id, public_id, created_at
FROM customer
WHERE status = 'ACTIVE'
ORDER BY created_at DESC, customer_id DESC
OFFSET :offset ROWS FETCH NEXT :page_size ROWS ONLY;

\echo '=== disjointness: page 0 vs page 1 must share no ids, expect 0 rows ==='
WITH page0 AS (
  SELECT customer_id FROM customer WHERE status = 'ACTIVE'
  ORDER BY created_at DESC, customer_id DESC
  OFFSET 0 ROWS FETCH NEXT 20 ROWS ONLY
), page1 AS (
  SELECT customer_id FROM customer WHERE status = 'ACTIVE'
  ORDER BY created_at DESC, customer_id DESC
  OFFSET 20 ROWS FETCH NEXT 20 ROWS ONLY
)
SELECT customer_id FROM page0
INTERSECT
SELECT customer_id FROM page1;

\echo '=== deep page: 5000 rows in ==='
\set offset 5000
EXPLAIN (ANALYZE, BUFFERS)
SELECT customer_id, public_id, created_at
FROM customer
WHERE status = 'ACTIVE'
ORDER BY created_at DESC, customer_id DESC
OFFSET :offset ROWS FETCH NEXT :page_size ROWS ONLY;

\echo '=== past the end: page 5000 of 20, only 35,001 ACTIVE rows exist ==='
\set offset 100000
EXPLAIN (ANALYZE, BUFFERS)
SELECT customer_id, public_id, created_at
FROM customer
WHERE status = 'ACTIVE'
ORDER BY created_at DESC, customer_id DESC
OFFSET :offset ROWS FETCH NEXT :page_size ROWS ONLY;
-- Lab 38 step 11 — keyset paging.
-- The guide's :last_created / :last_id binds are read from the data with
-- \gset rather than typed in, so the seek values are provably the boundary
-- rows of this dataset. Sort is DESC, DESC, so both seek operators are <.

\echo '=== boundary after page 0, row at position 20 ==='
SELECT customer_id AS last_id, created_at AS last_created
FROM customer WHERE status = 'ACTIVE'
ORDER BY created_at DESC, customer_id DESC
OFFSET 19 ROWS FETCH NEXT 1 ROWS ONLY \gset
\echo boundary: :last_created / :last_id

\echo '=== keyset page 1 ==='
EXPLAIN (ANALYZE, BUFFERS)
SELECT customer_id, public_id, created_at
FROM customer
WHERE status = 'ACTIVE'
  AND (created_at < :'last_created'::timestamptz
   OR (created_at = :'last_created'::timestamptz AND customer_id < :last_id))
ORDER BY created_at DESC, customer_id DESC
FETCH FIRST :page_size ROWS ONLY;

\echo '=== equivalence: keyset page 1 = offset page 1, both directions 0 rows ==='
WITH keyset AS (
  SELECT customer_id FROM customer
  WHERE status = 'ACTIVE'
    AND (created_at < :'last_created'::timestamptz
     OR (created_at = :'last_created'::timestamptz AND customer_id < :last_id))
  ORDER BY created_at DESC, customer_id DESC
  FETCH FIRST 20 ROWS ONLY
), offs AS (
  SELECT customer_id FROM customer
  WHERE status = 'ACTIVE'
  ORDER BY created_at DESC, customer_id DESC
  OFFSET 20 ROWS FETCH NEXT 20 ROWS ONLY
)
SELECT customer_id FROM keyset EXCEPT SELECT customer_id FROM offs;

WITH keyset AS (
  SELECT customer_id FROM customer
  WHERE status = 'ACTIVE'
    AND (created_at < :'last_created'::timestamptz
     OR (created_at = :'last_created'::timestamptz AND customer_id < :last_id))
  ORDER BY created_at DESC, customer_id DESC
  FETCH FIRST 20 ROWS ONLY
), offs AS (
  SELECT customer_id FROM customer
  WHERE status = 'ACTIVE'
  ORDER BY created_at DESC, customer_id DESC
  OFFSET 20 ROWS FETCH NEXT 20 ROWS ONLY
)
SELECT customer_id FROM offs EXCEPT SELECT customer_id FROM keyset;

\echo '=== walk: boundary at position 40, keyset page 2 ==='
SELECT customer_id AS last_id, created_at AS last_created
FROM customer WHERE status = 'ACTIVE'
ORDER BY created_at DESC, customer_id DESC
OFFSET 39 ROWS FETCH NEXT 1 ROWS ONLY \gset
EXPLAIN (ANALYZE, BUFFERS)
SELECT customer_id, public_id, created_at
FROM customer
WHERE status = 'ACTIVE'
  AND (created_at < :'last_created'::timestamptz
   OR (created_at = :'last_created'::timestamptz AND customer_id < :last_id))
ORDER BY created_at DESC, customer_id DESC
FETCH FIRST :page_size ROWS ONLY;

\echo '=== walk: boundary at position 60, keyset page 3 ==='
SELECT customer_id AS last_id, created_at AS last_created
FROM customer WHERE status = 'ACTIVE'
ORDER BY created_at DESC, customer_id DESC
OFFSET 59 ROWS FETCH NEXT 1 ROWS ONLY \gset
EXPLAIN (ANALYZE, BUFFERS)
SELECT customer_id, public_id, created_at
FROM customer
WHERE status = 'ACTIVE'
  AND (created_at < :'last_created'::timestamptz
   OR (created_at = :'last_created'::timestamptz AND customer_id < :last_id))
ORDER BY created_at DESC, customer_id DESC
FETCH FIRST :page_size ROWS ONLY;

\echo '=== the comparison: keyset at position 5000 vs OFFSET 5000 ==='
SELECT customer_id AS last_id, created_at AS last_created
FROM customer WHERE status = 'ACTIVE'
ORDER BY created_at DESC, customer_id DESC
OFFSET 4999 ROWS FETCH NEXT 1 ROWS ONLY \gset
EXPLAIN (ANALYZE, BUFFERS)
SELECT customer_id, public_id, created_at
FROM customer
WHERE status = 'ACTIVE'
  AND (created_at < :'last_created'::timestamptz
   OR (created_at = :'last_created'::timestamptz AND customer_id < :last_id))
ORDER BY created_at DESC, customer_id DESC
FETCH FIRST :page_size ROWS ONLY;

-- Failure experiment 3 — ORDER BY created_at without the customer_id
-- tiebreak. The bulk generator happens to make unique timestamps, but the
-- lab 37 seed inserted both fixtures in one transaction and CURRENT_TIMESTAMP
-- is the transaction start time, so Amina and Ravi are a real tie: their
-- relative order under ORDER BY created_at alone is unspecified and may
-- differ between plans, which is how page peers shuffle.
\echo '=== failure experiment 3: the fixtures tie on created_at ==='
SELECT public_id, created_at,
       COUNT(*) OVER (PARTITION BY created_at) AS rows_sharing_ts
FROM customer
WHERE public_id IN ('CUS-1001', 'CUS-1002')
ORDER BY created_at DESC;

-- The plans above show the guide's OR-form seek predicate does NOT seek in
-- PostgreSQL: Index Cond carries only status, the OR lands in Filter, and at
-- position 5000 the scan still produces and discards 5,000 rows — 5,047
-- buffers, the same cost as OFFSET 5000. PostgreSQL's index-friendly spelling
-- is the row-value comparison from this file's original TODO:
-- (created_at, customer_id) < (:ts, :id), which the planner can push into the
-- btree because both columns sit in the index with matching direction.

\echo '=== PostgreSQL-native seek: row-value form at position 5000 ==='
SELECT customer_id AS last_id, created_at AS last_created
FROM customer WHERE status = 'ACTIVE'
ORDER BY created_at DESC, customer_id DESC
OFFSET 4999 ROWS FETCH NEXT 1 ROWS ONLY \gset
EXPLAIN (ANALYZE, BUFFERS)
SELECT customer_id, public_id, created_at
FROM customer
WHERE status = 'ACTIVE'
  AND (created_at, customer_id) < (:'last_created'::timestamptz, :last_id)
ORDER BY created_at DESC, customer_id DESC
FETCH FIRST :page_size ROWS ONLY;

\echo '=== row-value form returns the same page as the OR form, 0 rows twice ==='
WITH rowform AS (
  SELECT customer_id FROM customer
  WHERE status = 'ACTIVE'
    AND (created_at, customer_id) < (:'last_created'::timestamptz, :last_id)
  ORDER BY created_at DESC, customer_id DESC
  FETCH FIRST 20 ROWS ONLY
), orform AS (
  SELECT customer_id FROM customer
  WHERE status = 'ACTIVE'
    AND (created_at < :'last_created'::timestamptz
     OR (created_at = :'last_created'::timestamptz AND customer_id < :last_id))
  ORDER BY created_at DESC, customer_id DESC
  FETCH FIRST 20 ROWS ONLY
)
SELECT customer_id FROM rowform EXCEPT SELECT customer_id FROM orform;

WITH rowform AS (
  SELECT customer_id FROM customer
  WHERE status = 'ACTIVE'
    AND (created_at, customer_id) < (:'last_created'::timestamptz, :last_id)
  ORDER BY created_at DESC, customer_id DESC
  FETCH FIRST 20 ROWS ONLY
), orform AS (
  SELECT customer_id FROM customer
  WHERE status = 'ACTIVE'
    AND (created_at < :'last_created'::timestamptz
     OR (created_at = :'last_created'::timestamptz AND customer_id < :last_id))
  ORDER BY created_at DESC, customer_id DESC
  FETCH FIRST 20 ROWS ONLY
)
SELECT customer_id FROM orform EXCEPT SELECT customer_id FROM rowform;
