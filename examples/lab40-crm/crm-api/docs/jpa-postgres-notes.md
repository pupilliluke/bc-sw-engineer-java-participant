# Lab 39 — JPA and PostgreSQL notes

Spring Data JPA over the Lab 37 schema, with Flyway owning the DDL and
`ddl-auto: validate` holding the entities to it. The database is `crm39` in the
`crm-postgres-lab39` container, separate from Lab 38's so Flyway can build a
schema from empty rather than meet one that already exists.

## Run it

```
docker compose up -d                  # from examples/lab39-crm
mvn -o spring-boot:run                # from crm-api
```

No environment variables need exporting. `spring.config.import` reads
`../.env`, the same file docker compose reads, so the password lives in one
place. A real environment variable of the same name still wins, which matters
if a shell was seeded from another lab's `.env`.

`mvn clean verify` runs 15 unit tests plus 7 integration tests. Failsafe is
configured explicitly: Surefire skips `*IT` by convention, so without it
`verify` reported green while never running `CustomerRepositoryIT`.

## Deviations from the guide

The step 5 and 6 entity templates do not start. Each change is commented in the
file it affects.

| Template | Problem | What it became |
| --- | --- | --- |
| `@Enumerated(EnumType.STRING)` on `String status` | only valid on an enum field, throws at startup | annotation removed, field stays `String` |
| `@Column(name = "customer_id")` on the `@ManyToOne` | associations are not columns | `@JoinColumn` |
| `@Column(name = "BALANCE", precision = 19, scale = 2)` over a `long` | `precision`/`scale` are BigDecimal attributes and mean nothing on a `long` | `BigDecimal balance`, with `V2` moving the column to `NUMERIC(19,2)` |
| no `full_name`, no `account.status` | both `NOT NULL` in V1, so inserts fail | mapped |

Fixtures are seeded by `FixtureLoader`, not by a migration. A migration that
inserts Amina and Ravi would put lab data into every environment that ever runs
it; the loader is idempotent by `public_id`.

`Customer`, the Lab 25 model class, is no longer used by the service or the
controller. `CustomerResponse` and `CreateCustomerRequest` are the API shapes,
and `id`/`name` still map to `public_id`/`full_name` as they have since Lab 34.

## Failure experiments

| # | Experiment | Observed | Restore |
| - | ---------- | -------- | ------- |
| 1 | `docker stop crm-postgres-lab39`, then start the app | fails fast at `flywayInitializer`: `Connection to localhost:5432 refused`. No partial start, no serving with a dead pool | `docker start crm-postgres-lab39` |
| 2 | POST a customer with `amina@example.com` | HTTP 409, ProblemDetail `{"title":"Conflict","detail":"Email already registered","correlationId":"lab-request-001"}`. No SQLSTATE, constraint name or table name in the body | use a fresh email |
| 3 | Stale `@Version` update | `CustomerRepositoryIT.staleVersionFails`: the second writer's `WHERE version = stale` matches no row and is rejected rather than overwriting | reload, reapply |
| 4 | `GET /api/customers/page?size=1000` | `page.size` came back `100`. `Math.min(Math.max(size, 1), 100)` caps it before `PageRequest` sees it | keep the cap |
| 5 | `ddl-auto: update` with Flyway on | not left enabled. Two owners of the schema: Flyway applies V1, Hibernate then adds whatever it thinks is missing, and the result matches no file anyone can read. `validate` is the setting that makes the split enforceable | `validate` |

Experiment 1 is worth reading twice: the failure is at Flyway, before Hibernate
or Tomcat. A database that is unreachable at boot stops the application rather
than producing one that answers requests with connection errors.

## Evidence

`notes/screenshots/lab-39/` holds the verify output and the experiment
transcripts. No passwords: `.env` is gitignored, `.env.example` carries key
names with the two password values blank.
