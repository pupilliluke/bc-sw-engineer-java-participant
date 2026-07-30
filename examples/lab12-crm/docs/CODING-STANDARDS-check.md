# Coding standards self-check — Lab 12

Step 8. Checked against `CustomerService.java` after the refactor, not against
intent. `mvn -B verify` green at the time of writing, 8 tests.

| # | Confirm | Your notes |
| - | ------- | ---------- |
| 1 | Meaningful type and method names | **Pass** — `createCustomer` / `getCustomer` / `updateStatus`, field `customersById`, helpers `requireNonBlank` / `requireUniqueId` / `requireExisting`. No `doStuff`, no `data`, no single-letter parameters. Verified by grep: the only `doStuff` hit in `src/` is inside `CustomerService.before.java.txt`, the frozen snapshot, which is not compiled. |
| 2 | No raw types in new code | **Pass** — `Map<String, Customer> customersById`. No cast appears anywhere in the class; the baseline had four `(Customer)` casts. |
| 3 | Validation in clear helpers | **Pass** — three named helpers, and each rule lives in exactly one of them. `getCustomer` delegates to `requireExisting`, which calls `requireNonBlank`, so the blank-id check is written once. An earlier pass had it in three places; fixed before this check was signed off. |
| 4 | Exceptions instead of null for errors | **Pass** — `IllegalArgumentException` for blank and unknown, `IllegalStateException` for duplicate. No method returns `null`; the baseline returned it from three different failures. Every message carries `correlationId=lab-request-001`. |
| 5 | No production secrets / no PII beyond lab sample emails | **Pass** — fixtures only: `CUS-1001` Amina Khan, `CUS-1002` Ravi Singh, `CUS-1003` Priya Patel, `example.com` addresses, `555-01xx` phones. No token, key, or password in source, tests, or docs. `application.properties` is a placeholder with no values. |
| 6 | Service still compiles without Spring/JPA/Kafka | **Pass** — grep over `src/` for `springframework`, `jakarta.persistence`, `javax.persistence`, `kafka` returns exactly one hit, a comment in `Customer.java` recording that a `jakarta.persistence` suggestion was rejected. Not an import — same line Lab 10 logged. Imports in `CustomerService` are `java.time.LocalDateTime`, `java.util.Map`, `java.util.HashMap`, plus own-project entity and DTO types. `pom.xml` carries JUnit at `test` scope only. |

## Two standards notes beyond the table

**Lab 8 message wording.** The exception text differs from the Lab 10/11
service: this class says `Customer id already exists: CUS-1001` where Lab 11
said `Duplicate customerId: CUS-1001`, and `customerId must be provided` where
Lab 11 said `customerId must not be blank`. Nothing asserts on the strings, so
neither is wrong, but the divergence is recorded here rather than discovered in
Lab 13.

**What is deliberately not done.** No `CustomerRepository` interface behind the
service, and no interface segregation on the service itself. Both are documented
defers from the pre-lab SOLID scope note, not oversights — reasoning in
`notes/Week 2/Module 12/lab12-solid-scope.md` and summarised in this project's
README.
