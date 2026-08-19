# Lab 39 — Fill JPA TODOs

## Step 1 — Paste

```java
@Entity
@Table(name = "customer")
class CustomerEntity {
  @Id
  private String customerId;
  @Column(name = "full_name", nullable = false)
  private String fullName;
  @Column(nullable = false)
  private String status;
  @Version
  private long version; // optimistic lock
}

interface CustomerRepository extends JpaRepository<CustomerEntity, String> {
  Page<CustomerEntity> findByStatus(String status, Pageable pageable);
}

// application.yml ideas
spring.datasource.url: jdbc:postgresql://localhost:5432/crm
spring.jpa.hibernate.ddl-auto: validate
spring.flyway.enabled: true

// TODO: service.load("CUS-1001") -> Optional<CustomerEntity.java> for Amina
```

## Step 2 — Fill

`customerId`, `fullName`, `version`, `Pageable`, the postgresql URL,
`validate`, `true`.

| Blank | Fill | Why |
| --- | --- | --- |
| `@Id` field | `customerId` | the starter's own naming |
| full_name field | `fullName` | camelCase field against the snake_case column |
| `@Version` field | `version` | the counter Hibernate bumps on every update |
| repository parameter | `Pageable` | what turns findByStatus into a page rather than the whole list |
| datasource url | `jdbc:postgresql://localhost:5432/crm` | the lab 37 and 38 database, not northstar |
| ddl-auto | `validate` | Hibernate checks the schema and changes nothing |
| flyway.enabled | `true` | Flyway owns the schema |

three of these fills contradict the schema this lab maps, and the
contradiction is the point rather than a mistake to paper over.

`@Id private String customerId` assumes the CUS- value is the primary
key. in the lab 37 DDL it is not, customer_id is BIGINT identity and
`CUS-1001` is public_id, so the entity I actually write has
`@Id @GeneratedValue private Long customerId` plus a separate
`publicId`, and the repository is typed `<CustomerEntity, Long>`. the
deck's repository slide agrees, it declares `findByPublicId(String)`
against a Long-keyed repository.

`@Version private long version` maps to a column named `version` by
default. lab 37 named the column `version_no`, so the real field carries
`@Column(name = "version_no")` with it.

the datasource URL is the suggested `northstar` in the deck. this CRM's
database has been `crm` since lab 37 and the container publishes 5432,
so the URL is the one above. no password in it, and no password in
application.yml at all, that comes from the environment.

`validate` over `none` because validate is the setting that catches the
divergence. Flyway applies the migration, Hibernate compares the entity
against what is there, and a field that no longer matches a column stops
the application at startup instead of at the first query. `create` or
`create-drop` against this database would drop 50,002 rows and the two
fixtures with them.

## Step 3 — Usage TODO

`// TODO: service.load("CUS-1001") -> Optional<CustomerEntity> for Amina`
is the last line of the block above.

`Optional` because the answer can legitimately be nothing, an id that
does not exist is a 404 rather than an error, and Optional makes the
caller handle that instead of a null arriving somewhere later. the
service is what turns the empty Optional into the 404, not the
repository.

the lookup goes through publicId, so behind `service.load("CUS-1001")`
is `repository.findByPublicId("CUS-1001")` and not `findById`.

## Step 4 — Locking note

two concurrent updates to Ravi's status: the first commits and bumps
`version_no` from 0 to 1, the second was holding version 0, its UPDATE
carries `WHERE version_no = 0`, matches no row, and Hibernate raises an
optimistic lock exception rather than silently overwriting the first
change.

this is why the column was in the lab 37 DDL from the start. the lost
update it prevents is the case where two operators open Ravi at the same
time, one sets ACTIVE and one sets CLOSED, and without the version check
whichever saves second wins with no trace that the other happened.

the exception is a conflict, so the service maps it to HTTP 409, the
same status a duplicate email gets. that is the other half of the
prediction: `email_normalized` is UNIQUE, an insert with a taken address
raises SQLSTATE 23505, Spring surfaces it as
DataIntegrityViolationException, and the service catches it and returns
409 rather than letting a 500 out. lab 37's negative tests already
proved 23505 fires; this lab decides what the API does with it.

catching the exception is the fallback rather than the plan.
`existsByEmailNormalized` from the repository sketch checks first, and
the catch is there for the case where another request inserts between
the check and the insert, which the constraint is the only thing that
can actually stop.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab39-todos.md`
- [ x ] Entity blanks filled
- [ x ] Datasource/Flyway blanks filled
- [ x ] Version semantics noted
