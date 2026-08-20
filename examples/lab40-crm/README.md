Northstar CRM on PostgreSQL with Spring Data JPA (Lab 39)

crm-api is the Spring Boot service, crm-ui is the React front end carried
forward from lab 36 and untouched by this lab. compose.yaml runs PostgreSQL 17
in the container crm-postgres-lab39 against the database crm39, and Flyway owns
the schema from crm-api/src/main/resources/db/migration.

The database is crm39 rather than lab 38's crm because Flyway's first run has
to build a schema from empty. Pointing it at the lab 38 database would meet
tables that already exist, with no flyway_schema_history to explain them.

START POSTGRES

  cp .env.example .env      # then set the two blank passwords
  docker compose up -d
  docker compose ps         # wait for healthy

  # lab 38 holds the container name crm-postgres and port 5432, so stop it
  # first: docker stop crm-postgres-lab38

CREATE THE APPLICATION ROLE

  # roles are cluster-wide, so a fresh container has no crm_app
  psql -h localhost -U crm_admin -d crm39 \
    -v dbname=crm39 -v crm_app_password="$CRM_APP_PASSWORD" \
    -f ddl/01_create_user.sql

RUN THE API

  cd crm-api
  mvn -o spring-boot:run

  # no exports needed: spring.config.import reads ../.env, the same file
  # docker compose reads. A real environment variable of the same name still
  # wins, so use a shell that was not seeded from another lab's .env.

  # Flyway applies V1 on first start, then ddl-auto: validate holds the
  # entities to it. FixtureLoader seeds CUS-1001 and CUS-1002 if absent.

TEST

  mvn -o clean verify       # 15 unit tests + 7 integration tests
  mvn -o test -Dtest=CustomerRepositoryIT

  The integration tests run against the real PostgreSQL above, not H2. Each is
  transactional and rolled back, so the fixtures survive.

ENDPOINTS

  POST /api/auth/login                          agent1/agent1 or admin1/admin1
  GET  /api/customers                           unbounded, kept for lab 25 parity
  GET  /api/customers/page?status=&page=&size=  bounded, size capped at 100
  GET  /api/customers/{publicId}
  POST /api/customers                           409 on duplicate email
  PUT  /api/customers/{publicId}
  PATCH /api/customers/{publicId}/status        409 on illegal transition

  All except /api/auth/login need a bearer token.

FILES

  compose.yaml            PostgreSQL 17, container crm-postgres-lab39
  .env.example            key names, both passwords blank
  ddl/01_create_user.sql  crm_app role, database name passed as :dbname
  ddl/03_seed.sql         lab 37 fixtures, kept for reference
  crm-api/.../db/migration/V1__crm_schema.sql
                          customer, account, and the two indexes lab 38 earned
  crm-api/docs/jpa-postgres-notes.md
                          deviations from the guide and the failure experiments

SECRETS

  .env holds real values and is gitignored. .env.example is committed with key
  names and no passwords. Nothing in application.yml carries a credential:
  CRM_APP_PASSWORD has no default, so a missing value fails at startup rather
  than falling back to something.
