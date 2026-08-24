# Lab 43 — CI runbook

## Two folders

Work in **`java-bootcamp`**. Workflow file: **`.github/workflows/crm-ci.yml`** at the repo root (not under `examples/lab43-crm/.github/`).

GitHub reads workflow definitions from exactly one place: `.github/workflows/` at the repository root, on the default branch. A workflow nested inside the project folder is an inert YAML file — no error, no warning, no run. The failure mode is silence.

Maven paths are `examples/lab43-crm/crm-api`, not `examples/lab43-crm`. This project keeps the Maven module one level down, alongside `compose.yaml` and `ddl/`. Four settings in the workflow depend on that: `defaults.run.working-directory`, both `cache-dependency-path` entries, and the artifact paths.

## Pipeline policy

| Trigger | Jobs | Notes |
| ------- | ---- | ----- |
| Pull request | verify | Fast feedback; no `crm-jar` |
| `main` push | verify + package | Immutable JAR + checksum |
| Tag `v*` | verify + package | Release candidate identity |
| Deploy | none | Lab 44 |

`package` is gated by `if: github.ref == 'refs/heads/main' || startsWith(github.ref, 'refs/tags/')` and `needs: verify`. A JAR built from a pull request branch has no destination, so building one is waste.

## Secrets / variables

Names only. Values are never written here, in the workflow, or in any tracked file.

| Name | Used by | Required |
| --- | --- | --- |
| `JWT_SECRET` | verify job `env:` | **Yes** — the build fails without it |
| `NVD_API_KEY` | Dependency-Check step | No — that step is commented out |

`JWT_SECRET` is not optional and is not in the course baseline. This build carries Spring Security, and `application.yml` declares `northstar.security.jwt-secret: ${JWT_SECRET}` with no default. Locally the value arrives through `spring.config.import` from `.env`, which is gitignored and therefore absent on a runner. Without the secret, every test that loads a Spring context fails — 19 of 26.

Registry tokens and kubeconfig wait for Lab 44. Nothing in this workflow holds a deploy credential.

## Verify job

- `actions/setup-java@v4`, Temurin **21**, `cache: maven` keyed on `examples/lab43-crm/crm-api/pom.xml`
- `services: postgres` — `postgres:16`, user `crm`, database `crm_lab43`, `pg_isready` health check
- `mvn -B -ntp clean verify`. Never `-DskipTests`; skipping tests in verify makes the gate decorative
- Surefire reports uploaded with `if: always()`

The Postgres service is required, not optional. Seven of the 26 tests are `CustomerRepositoryIT` and open a real connection. Without the service they cannot run, and a green build would mean 19 of 26 — a gate reporting success on 73 percent of itself.

`SPRING_DATASOURCE_URL`, `_USERNAME` and `_PASSWORD` in the job `env:` override `application.yml` outright. OS environment variables sit above `application.yml` in Spring Boot's property order, so the `${CRM_DB_URL:...}` placeholder in the yml is never evaluated.

## Re-run failed verify

1. Open Actions → the failed run
2. **Re-run failed jobs** for an infrastructure or cache flake; **Re-run all jobs** if checkout or the toolchain was at fault. If a test failed, neither helps — fix it locally first, because a real failure fails identically in both places
3. Confirm the Surefire artifact uploaded. It does even on failure, because of `if: always()`
4. Local equivalent, from `examples/lab43-crm/crm-api`:

```
mvn -B -ntp clean verify
```

Expect 26 tests, 19 unit and 7 integration, roughly 1m20s. The local run needs `crm-postgres-lab41` up and `.env` pointing at `crm_lab43`.

CLI equivalents:

```
gh run list --workflow=crm-ci.yml
gh run view <run-id> --log-failed
gh run rerun <run-id> --failed
```

## Failure experiment (performed)

`CustomerServiceTest.closedIsTerminal` was inverted — `assertThrows(IllegalStateException.class, ...)` replaced with `assertDoesNotThrow(...)`, asserting that a CLOSED customer accepts a transition to ACTIVE, which contradicts the transition table.

Observed:

| | Green run | Red run |
| --- | --- | --- |
| verify | success | **failure** — `CustomerServiceTest.closedIsTerminal` |
| package | success | **skipped** |
| `test-reports` | 17,660 bytes | **18,446 bytes** |
| duration | 4m43s | 1m22s |

Three things worth keeping. `package` was **skipped**, not failed — `needs: verify` means it never started, so there is no failure to attribute to it. The reports uploaded anyway; `if: always()` is what makes a red build diagnosable without re-running it. The red run was faster, because it died before `package` could spend its 2m39s.

Restored by reverting the single assertion. The test file is byte-identical to the Lab 41 original either side of the change, which is what makes the red run evidence rather than an anecdote.

## Schema portability

`CustomerRepositoryIT` originally issued raw SQL against `crm_app.customer`. That schema exists locally only because the Lab 37 DDL created it and set the role's `search_path`. A fresh CI Postgres has neither, so the qualified name could not resolve and the test failed both locally against `crm_lab43` and in CI.

Fixed by dropping the qualifier: `UPDATE customer SET ...`. The Flyway migrations create tables unqualified, the JPA entity maps unqualified, and the test now matches — everything resolves through `search_path`, so nothing has to agree on a schema name. The Lab 41 copy is unchanged.

## Artifact identity for Lab 44

- JAR + `SHA256SUMS` + `GITHUB_SHA` from artifact **`crm-jar`**
- Produced on `main` / `v*` only
- Lab 44 must **download** this artifact — do not `mvn package` again

Rebuilding on the deploy agent produces a file no test ever saw, from a working tree that may not match the commit under test, and the checksum recorded in CI then describes something nobody is running. Lab 41 showed how quickly identity drifts: rebuilding the same source produced a different manifest digest every time, because the provenance attestation varies per build. Identity is captured once and carried, never recomputed and assumed equal.

## Residual risks

- **Dependency-Check is not enforced.** The step is commented out. The Lab 40 gate fails at `failBuildOnCVSS 7` with 70 findings across 11 Spring Boot 3.3.5 managed transitives, all triaged with owners in `examples/lab40-crm/docs/security-findings.csv`. Enabling it unconditionally would make CI red on every run. The threshold that is actually enforced today is: none.
- **Pinned actions are deprecated.** `actions/setup-java@v4` is superseded by v5, and the Node 20 actions are being forced onto Node 24. Both are warnings, not failures. The starter pins v4.
- **`JWT_SECRET` has no rotation owner recorded.** It exists as a repository secret and in a local gitignored `.env`. Nothing tracks when it was set or who rotates it.
- **No branch protection.** `main` accepts direct pushes, so verify can fail on `main` itself rather than being caught on a pull request first.
