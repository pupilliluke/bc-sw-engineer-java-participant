# Lab 43 — Outline CI Runbook

## Step 1 — Headings

`docs/ci-runbook.md` carries:

| Heading | Contents |
| --- | --- |
| Policy | triggers and the job matrix: verify on every event, package on `main` and `v*` tags, deploy nowhere yet |
| Jobs | what `verify` and `package` each run, and why `package` skips tests |
| Reports | where Surefire and Failsafe land, and that they upload on failure too |
| Re-run | the GitHub UI and CLI paths, plus the local equivalent |
| Gates | Surefire and Failsafe green; the Dependency-Check threshold that is actually enforced |
| Secrets | names only, never values, of the secured variables the workflow reads |
| Deploy | none yet. Lab 44 |

## Step 2 — Re-run recipe

GitHub UI: Actions tab, open the failed run, **Re-run failed jobs** to retry only
what broke, or **Re-run all jobs** when the cause was the checkout or the
toolchain rather than a test.

GitHub CLI:

```
gh run list --workflow=crm-ci.yml
gh run rerun <run-id> --failed
gh run view <run-id> --log-failed
```

Local equivalent, from `examples/lab43-crm`:

```
mvn -B clean verify
```

Expect 26 tests, 19 unit and 7 integration. The local run needs PostgreSQL up,
because `CustomerRepositoryIT` opens a real connection; on the runner that comes
from a service container instead. Re-running in CI before reproducing locally
usually wastes a cycle, since a genuine test failure fails identically in both.

## Step 3 — Evidence index

| Artifact | Name | Holds |
| --- | --- | --- |
| Test reports | `test-reports` | `surefire-reports/` and `failsafe-reports/`, uploaded with `if: always()` |
| Packaged JAR | `crm-jar` | the JAR and `target/SHA256SUMS` with the commit id and run number |

`crm-jar` is what Lab 44 downloads. It is the identity link between what was
tested and what gets promoted.

## Step 4 — Scope

Pre-lab outline for Lab 43. The workflow is not committed, no run has been
triggered, and no gate has been proven green here. Writing
`.github/workflows/crm-ci.yml` and getting it passing is Lab 43 itself.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [x] File exists at `notes/lab43-ci-runbook-outline.md`
- [x] Headings complete
- [x] Re-run recipe present
- [x] Pre-lab marked
