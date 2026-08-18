-- Lab 37 step 12 — cleanup in dependency order. Run as crm_app:
--   psql -f database/05_drop.sql
-- Children before the parent. The GUIDE prints Oracle's
-- DROP TABLE ... CASCADE CONSTRAINTS PURGE, which PostgreSQL does not accept;
-- the PostgreSQL spelling is DROP TABLE ... CASCADE and there is no recycle
-- bin to purge. See database/design-decisions.md for the full dialect list.

\set ON_ERROR_STOP on
SET search_path = crm_app;

DROP TABLE IF EXISTS customer_status_history CASCADE;
DROP TABLE IF EXISTS address CASCADE;
DROP TABLE IF EXISTS account CASCADE;
DROP TABLE IF EXISTS customer CASCADE;

\echo '== tables remaining in crm_app after drop =='
SELECT table_name FROM information_schema.tables WHERE table_schema = 'crm_app';
