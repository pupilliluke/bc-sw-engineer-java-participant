# Lab 39 prep checklist

## Earlier exercise files present?

| File | Present? (yes/no) |
| ---- | ----------------- |
| notes/lab39-jpa.md | yes |
| notes/lab39-repository-sketch.md | yes |
| notes/lab39-todos.md | yes |
| notes/lab39-paging-locking.md | yes |
| notes/lab39-flyway-plan.md | yes |

kept under notes/Week 4/Module 39/ with the rest of the module notes.

## Stack

| Piece | Version on this machine |
| --- | --- |
| JDK | Temurin 21.0.4 LTS |
| Maven | 3.9.9 |
| Spring Boot | 3.x, with data-jpa, flyway-core, flyway-database-postgresql, postgresql runtime |
| PostgreSQL | 17.6 client, postgres:17 in Docker |
| Docker | 27.3.1 |

the two Flyway artifacts are separate. flyway-core alone does not cover
PostgreSQL 17 in Boot 3.x, flyway-database-postgresql is the one that
does, and a missing runtime `postgresql` driver is the deck's
driver-not-found row.

## Schema readiness

lab 37 built it and lab 38 tuned it, so the columns this lab maps are
settled: public_id, full_name, email_normalized, phone, status,
version_no, created_at, updated_at on customer, and account with a
NUMERIC(19,2) balance and an FK to customer. indexes are
uk_customer_public, uk_customer_email, ix_account_customer,
ix_address_customer, ix_history_customer_time and
ix_customer_status_created.

lab 38's database is the one to map, and it still holds 50,002 generated
customers alongside the fixtures. that is useful, a page of twenty out
of fifty thousand behaves like the real thing while a page of twenty out
of two proves nothing, and it means `ddl-auto: create-drop` would cost
the whole measured data set.

the container is `crm-postgres-lab38` on port 5432, not `crm-postgres`,
because lab 37 holds that name. lab 39's compose will need its own name
for the same reason.

## Secrets hygiene

`${CRM_DB_PASSWORD}` from the environment, per the deck's config slide.
no password in application.yml, none in the datasource URL, none in a
migration. the URL and username can carry defaults in the file,
`${CRM_DB_URL:jdbc:postgresql://localhost:5432/crm}` and
`${CRM_DB_USERNAME:crm_app}`, because neither is a secret; the password
gets no default so a missing environment variable fails loudly rather
than falling back to something.

.env holds the real values and is gitignored, .env.example is committed
with the key names and no values. same rule as labs 37 and 38, and the
same one lab 36 applied to the Vite env.

the application connects as `crm_app`, the least-privilege role from lab
37, not as `crm_admin`. Flyway runs its migrations as the same role,
which is why lab 37 granted it CREATE on its own schema.

## Hypothesis

Hibernate `validate` will fail on the first run rather than pass. the
entity fields in lab39-jpa.md were written from the DDL, but `version_no`
against a default-named `version`, `Instant` against TIMESTAMPTZ and the
CHAR(3) currency are each a place where the mapping and the column can
disagree in a way only startup will reveal. that is the setting working,
not the setting being wrong.

the paged ACTIVE list should come back as an Index Scan on
ix_customer_status_created if the Sort matches the index order, and as a
Seq Scan plus a sort of 35,001 rows if it does not. that is a
lab-38-shaped measurement available from the application side.

## Real PostgreSQL only

no H2 standing in for PostgreSQL in the integration tests. H2 does not
have TIMESTAMPTZ, its NUMERIC and identity behaviour differ, and a
CHECK-constrained status enum plus a unique violation surfacing as
SQLSTATE 23505 is exactly the behaviour the 409 path depends on. a green
test against H2 would prove the code runs, not that it runs against this
database. Testcontainers or the compose instance.

verify cannot pass without a real PostgreSQL for the same reason
`ddl-auto: validate` is the point of the exercise: there is nothing to
validate against unless the schema Flyway built is really there.

## Next lab preview

lab 40 is Week 5 and application security testing. parked, this module
finishes Week 4 and the CRM's persistence story ends here, entity to
repository to service with the schema owned by migrations.

## Scope statement

Pre-lab only — prepare for lab; do not complete full Lab 39 now.

## Self mark

Overall prep: Pass
If Fail, revisit exercise(s): n/a
