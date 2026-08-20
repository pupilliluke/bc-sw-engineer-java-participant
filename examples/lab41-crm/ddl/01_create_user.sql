-- Lab 37 step 4 — least-privileged application role.
-- Run as the bootstrap superuser (crm_admin) against the crm database:
--   psql -v crm_app_password="$CRM_APP_PASSWORD" -f database/01_create_user.sql
-- The password arrives as a psql variable, so it never sits in this file.

\set ON_ERROR_STOP on

-- Idempotent create: the statement is only generated when the role is absent.
SELECT format('CREATE ROLE crm_app LOGIN PASSWORD %L', :'crm_app_password')
WHERE NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'crm_app')
\gexec

-- No SUPERUSER, no CREATEDB, no CREATEROLE, no BYPASSRLS. Say it explicitly
-- so a later ALTER cannot quietly widen the role without showing in the diff.
ALTER ROLE crm_app NOSUPERUSER NOCREATEDB NOCREATEROLE NOBYPASSRLS NOREPLICATION;

-- Lab 37 hardcoded "crm" here. Lab 39's database is crm39, so the name comes
-- in as a psql variable instead of being swapped for another literal:
--   psql -v dbname=crm39 -f ddl/01_create_user.sql
GRANT CONNECT ON DATABASE :"dbname" TO crm_app;

CREATE SCHEMA IF NOT EXISTS crm_app AUTHORIZATION crm_app;
GRANT USAGE, CREATE ON SCHEMA crm_app TO crm_app;

-- crm_app owns its own schema and nothing else. public stays closed to it.
REVOKE ALL ON SCHEMA public FROM crm_app;

ALTER ROLE crm_app SET search_path = crm_app;

-- Evidence: the privilege flags that must all be false.
SELECT rolname, rolsuper, rolcreatedb, rolcreaterole, rolbypassrls, rolcanlogin
FROM pg_roles
WHERE rolname IN ('crm_admin', 'crm_app')
ORDER BY rolname;
