# Lab 43 — Plan JDK 21 Verify Job

## Step 1 — Setup

Actions steps, in order:

1. `actions/checkout@v4`
2. `actions/setup-java@v4` with `distribution: temurin`, `java-version: "21"`,
   `cache: maven`, `cache-dependency-path: examples/lab43-crm/pom.xml`
3. `mvn -B clean verify` with `working-directory: examples/lab43-crm`

The cache key points at the pom rather than the repository root because the
build lives in a subdirectory and the root has no pom to hash.

One thing this project needs that the step list does not mention:
`CustomerRepositoryIT` requires a live PostgreSQL. Locally that is the
`crm-postgres-lab41` container. On a runner there is no such container, so the
verify job needs a `services:` block running a postgres image with the database,
role and password the tests expect, or the 7 integration tests cannot run and
`verify` is not the gate it claims to be. This is the same constraint that made
the Lab 41 Dockerfile run `package` instead of `verify`.

## Step 2 — Check the reference

Upload Surefire and Failsafe reports with `if: always()`, so the evidence
survives the run that failed. A red build with no reports is a build nobody can
diagnose without re-running it.

Paths: `examples/lab43-crm/target/surefire-reports/` and
`examples/lab43-crm/target/failsafe-reports/`.

## Step 3 — Failure drill plan

Break one assertion in `CustomerServiceTest`, the smallest of the unit tests —
`closedIsTerminal` asserts that a CLOSED customer rejects every transition.
Invert one expected value, commit on a branch, open a pull request.

Expected: the verify job fails, the Actions summary shows the failing test by
name, and the uploaded Surefire report contains the assertion message. The
important part is that the report uploads despite the failure, which is what
`if: always()` is for.

Restore by reverting the single line and confirming the same job goes green with
26 tests. The test file must be identical either side of the change, or the
green run proves nothing about the red one.

## Step 4 — Local habit

Before pushing:

```
java -version
mvn -v
mvn -B clean verify
```

`java -version` must report 21. Running verify locally first keeps the runner
from being used as a compiler, and the local run is where the PostgreSQL
container is already available.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [x] File exists at `notes/lab43-java21-verify.md`
- [x] JDK 21 + `mvn` verify listed
- [x] Report upload planned
- [x] Failure drill described
