Northstar CRM PostgreSQL schema (Lab 37)

One project, no application code. database/ holds the numbered SQL scripts,
compose.yaml runs PostgreSQL 17 with a named volume, and the design notes
live in database/design-decisions.md. Lab 38 tunes queries on this schema
and lab 39 maps it with JPA, so the table, column and constraint names
here are the contract those labs inherit.

START POSTGRES

  cp .env.example .env      # then set real lab-only passwords
  docker compose up -d
  docker compose ps         # wait for healthy

  # data lives in the named volume crm_pgdata, it survives a restart

RUN THE SCRIPTS IN ORDER

  # 1. least-privilege role, as the bootstrap superuser crm_admin
  PGPASSWORD=$POSTGRES_PASSWORD psql -h localhost -U crm_admin -d crm \
    -v crm_app_password="$CRM_APP_PASSWORD" -f database/01_create_user.sql

  # 2-4. everything else as crm_app
  PGPASSWORD=$CRM_APP_PASSWORD psql -h localhost -U crm_app -d crm -f database/02_schema.sql
  PGPASSWORD=$CRM_APP_PASSWORD psql -h localhost -U crm_app -d crm -f database/03_seed.sql
  PGPASSWORD=$CRM_APP_PASSWORD psql -h localhost -U crm_app -d crm -f database/04_verify.sql

  # 5. drop in dependency order, then re-run 02 and 03 to prove repeatability
  PGPASSWORD=$CRM_APP_PASSWORD psql -h localhost -U crm_app -d crm -f database/05_drop.sql

SCRIPTS

  01_create_user.sql  crm_app role, no superuser, owns schema crm_app
  02_schema.sql       customer, account, address, customer_status_history
                      with named constraints, plus the three indexes
  03_seed.sql         Amina CUS-1001 ACTIVE with account and address,
                      Ravi CUS-1002 PROSPECT with neither
  04_verify.sql       positive selects, then five negative tests inside
                      savepoints, ends with no net change
  05_drop.sql         children before parent

FIXTURES

  CUS-1001  Amina Khan  ACTIVE    account ACCT-1001-01, 2500.00 CAD, HOME address
  CUS-1002  Ravi Singh  PROSPECT  no account, no address

The history row for Amina carries correlation lab-request-001, the same id
the Spring and React labs have used since lab 25.

DELIBERATE DIFFERENCES FROM THE GUIDE

1. The GUIDE's docker run sets POSTGRES_USER=crm_app, which makes crm_app
   the bootstrap superuser and contradicts step 4's least-privilege user.
   Here the container superuser is crm_admin and crm_app is created
   separately with NOSUPERUSER NOCREATEDB NOCREATEROLE NOBYPASSRLS.

2. 05_drop.sql uses DROP TABLE IF EXISTS ... CASCADE. The GUIDE prints
   Oracle's DROP TABLE ... CASCADE CONSTRAINTS PURGE, which PostgreSQL
   rejects, and there is no recycle bin to purge.

3. The negative tests record PostgreSQL SQLSTATE codes 23514, 23505, 23503
   and 23502, not the GUIDE's ORA-02290 / ORA-00001 / ORA-02291. The full
   mapping table is in database/design-decisions.md.

4. Two negative tests beyond the GUIDE's three: a NOT NULL violation, and
   deleting a customer that owns an account to prove ON DELETE RESTRICT.

5. Extra CHECK constraints the GUIDE does not list: ck_customer_email_lower
   so email_normalized is actually normalized, and ck_hist_transition so a
   history row cannot claim a status changed to itself.

6. compose.yaml pins postgres:17 to match the psql 17 client on this
   machine. The course starter compose pins 16; both work, the client and
   server majors just line up this way.

FULL PATH, NOT TIMED PATH

This follows the GUIDE's full steps: email_normalized, NUMERIC(19,2)
money, and the four-value status CHECK including SUSPENDED. The timed-path
starter contract (email, balance_cents BIGINT, three statuses) is a
different track and the two are not mixed.

CLEANUP

  docker compose stop        # keep the volume
  # docker compose down -v   # destructive, drops the data
  git status --short

.env is gitignored and holds every password, .env.example is committed
with placeholders. No real PII is seeded, the fixtures are fictional.

NOTES

Evidence is in java-bootcamp/notes/screenshots/lab-37/, the role
privileges, schema apply, seed verification, negative tests and the
drop/recreate proof. Checkpoints and reflection answers are in
notes/Week 4/Module 37/lab37-answers.md. Full GUIDE at
labs/Week 4 - Kafka, React, PostgreSQL and Resilience/module-37/lab37/.
