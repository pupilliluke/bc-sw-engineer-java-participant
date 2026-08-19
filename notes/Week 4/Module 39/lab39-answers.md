Lab 39 Spring Data JPA and PostgreSQL for the CRM (reflection questions,
checkpoints)

built as examples\lab39-crm on a copy of lab 36, with the lab 37 schema
rebuilt as a Flyway migration. PostgreSQL 17 in the container
crm-postgres-lab39 against the database crm39, separate from lab 38's so
Flyway's first run starts from empty. the work was V1__crm_schema.sql,
CustomerEntity and AccountEntity under ddl-auto validate, three focused
repository methods, a transactional service with DTO mapping, bounded
paging, 409 translation for both conflict kinds, and seven integration
tests against the real database.

the GUIDE's step 5 and 6 entity templates do not start. @Enumerated on a
String field, @Column on a @ManyToOne, and a column named BALANCE that V1
calls balance_cents. each replacement is commented in the file it
affects and listed in crm-api\docs\jpa-postgres-notes.md rather than
swapped silently.


REFLECTION QUESTIONS

1. Which design decision most affected correctness (types, OSIV, Flyway)?

Flyway owning the schema with ddl-auto set to validate. validate is what
turned every mapping mistake into a startup failure with the column name
in it rather than a wrong answer at the first query. it caught
@Column(name = "BALANCE") against a column actually called balance_cents,
which would otherwise have surfaced as a runtime error on the first
account read.

2. What evidence proves PostgreSQL mappings work (not just unit mocks)?

CustomerRepositoryIT, seven tests against the container rather than H2.
the identity column filled by the database, version starting at 0 and
moving on update, TIMESTAMPTZ returning an Instant, the unique constraint
raising DataIntegrityViolationException, two runs of the same page
returning identical ids, and the accounts collection still uninitialised
after a page load. mvn clean verify is 22 tests green. H2 would have
passed several of these for the wrong reasons.

3. Which failure was hardest to diagnose?

the entities compiling and the app starting while neither entity existed.
both files were missing their package declaration, so they landed in the
default package, outside the scan that starts at CrmApplication. Flyway
ran, validate passed because there was nothing to validate, and the app
reported healthy. the compiler cannot see it and the log does not mention
it. checking target\classes for where the .class files actually landed is
what found it.

the runner-up was password authentication failing while the same password
worked in psql. a real environment variable from lab 38, left in the
shell by an earlier .env sourcing, outranks spring.config.import.


CHECKPOINTS

| Checkpoint | Confirm | Result |
| --- | --- | --- |
| A1 | lab39-crm under examples | Pass, examples/lab39-crm with crm-api, crm-ui, ddl and compose |
| A2 | PostgreSQL healthy, JPA + postgresql + Flyway on classpath | Pass, crm-postgres-lab39 healthy, four dependencies resolve |
| A3 | env-based credentials, .env gitignored | Pass, spring.config.import reads ../.env, no default for the password, .env.example blank |
| B1 | Flyway V1 applied, ddl-auto validate | Pass, flyway_schema_history version 1 success t, validate green on every boot |
| B2 | both entities mapped, @Version present | Pass, version_no equivalent mapped as version, starts 0 and moves on update |
| B3 | lazy collections excluded from equality and JSON | Pass, equals on publicId, constant hashCode, accounts in neither toString nor any response DTO |
| C1 | repositories: publicId, email exists, status paging | Pass, findByPublicId, existsByEmail, findByStatus(String, Pageable) |
| C2 | transactional create/find, bounded deterministic paging | Pass, @Transactional on writes and readOnly on reads, size capped at 100, id tiebreak, adjacent pages disjoint in IT |
| C3 | 409 for duplicate and optimistic lock without raw database text | Pass, ProblemDetail with correlationId lab-request-001, no SQLSTATE or constraint name in the body |
| D1 | CustomerRepositoryIT and mvn clean verify green | Pass, 15 unit plus 7 integration, failsafe added because surefire skips *IT |
| D2 | README runbook complete | Pass, examples/lab39-crm/README.md plus crm-api/docs/jpa-postgres-notes.md |
| D3 | no secrets or target committed | Pass, .env gitignored, target and node_modules ignored, evidence is transcripts only |
