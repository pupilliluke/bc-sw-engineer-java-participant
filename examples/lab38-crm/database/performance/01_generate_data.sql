-- Lab 38 step 2 — representative volume.
-- Run as crm_app after the lab 37 DDL and seed:
--   psql -h localhost -U crm_app -d crm -f database/performance/01_generate_data.sql
--
-- The guide's block is Oracle PL/SQL (FOR .. LOOP, LPAD on a number,
-- NUMTODSINTERVAL, a bare / terminator). Kept at the bottom of this file for
-- comparison; the running version below is the set-based PostgreSQL form the
-- guide itself asks for under "Prefer set-based INSERT...SELECT".

\set ON_ERROR_STOP on
SET search_path = crm_app;

BEGIN;

-- 50,000 customers, ~70/30 ACTIVE/PROSPECT. CUS-1001 and CUS-1002 are not
-- touched, this only inserts CUS-BULK- rows alongside them.
--
-- created_at is anchored to a fixed date rather than now() so the same day
-- exists on every re-run. step 8 measures 2026-07-01, which is 48 days back
-- and inside the 90-day spread. the seconds term spreads rows through each
-- day so a half-open range has something to exclude at the boundary.
INSERT INTO customer (public_id, full_name, email_normalized, phone, status, created_at)
SELECT
  'CUS-BULK-' || lpad(i::text, 6, '0'),
  'Bulk User ' || lpad(i::text, 6, '0'),
  'user' || lpad(i::text, 6, '0') || '@example.test',
  '+1-555-' || lpad((i % 10000)::text, 4, '0'),
  CASE WHEN i % 10 < 7 THEN 'ACTIVE' ELSE 'PROSPECT' END,
  TIMESTAMPTZ '2026-08-18 00:00:00+00'
    - ((i % 90) * INTERVAL '1 day')
    + ((i % 86400) * INTERVAL '1 second')
FROM generate_series(1, 50000) AS i;

-- Accounts for every second bulk customer, so ~25,000 accounts and half the
-- customers with none. step 9 compares a selective join against a broad one
-- and needs both sides populated; the guide's step 2 block loads customers
-- only, which would leave the broad join with the single lab 37 fixture row.
INSERT INTO account (account_number, customer_id, account_type, status, balance, currency)
SELECT
  'ACCT-BULK-' || right(c.public_id, 6) || '-01',
  c.customer_id,
  (ARRAY['CHECKING', 'SAVINGS', 'CREDIT'])[1 + (c.customer_id % 3)],
  'OPEN',
  round((c.customer_id % 100000)::numeric / 10, 2),
  'CAD'
FROM customer c
WHERE c.public_id LIKE 'CUS-BULK-%'
  AND (right(c.public_id, 6))::int % 2 = 0;

COMMIT;

-- Expected: >= 50,002 customers, roughly 70/30, fixtures still present.
SELECT COUNT(*) AS cnt, status FROM customer GROUP BY status ORDER BY status;
SELECT COUNT(*) AS accounts FROM account;
SELECT public_id, full_name, status FROM customer
WHERE public_id IN ('CUS-1001', 'CUS-1002') ORDER BY public_id;

-- ---------------------------------------------------------------------------
-- Guide version, Oracle only, does not run on PostgreSQL. Kept for the diff.
--
-- BEGIN
--   FOR i IN 1..50000 LOOP
--     INSERT INTO customer (public_id, status)
--     VALUES (
--       'CUS-BULK-' || LPAD(i, 6, '0'),
--       CASE WHEN MOD(i, 10) < 7 THEN 'ACTIVE' ELSE 'PROSPECT' END
--     );
--   END LOOP;
--   COMMIT;
-- END;
-- /
