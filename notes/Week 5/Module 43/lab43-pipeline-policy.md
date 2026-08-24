# Lab 43 — Define Pipeline Triggers

## Reference

| Event | Verify | Package JAR+SHA |
| --- | --- | --- |
| pull_request | Yes | No (typical) |
| push main | Yes | Yes |
| tag v* | Yes | Yes |

## Step 1 — Matrix

| Event | verify | package | deploy |
| --- | --- | --- | --- |
| `pull_request` | yes | no | no |
| `push` to `main` | yes | yes | not yet, Lab 44 |
| `tag v*` | yes | yes | not yet, Lab 44 |

verify runs on every event, so nothing merges without the gate. package runs
only where the artifact could be promoted, because a JAR built from a pull
request branch has no destination. deploy is absent entirely; no job in this
workflow holds a deploy credential.

`mvn -B clean verify` on this project is 26 tests, 19 unit and 7 integration,
and takes 1m28s locally.

## Step 2 — Check the reference

PRs get fast feedback, main and tags get stronger gates, deploy credentials
never live in Git.

The two stronger gates this project already has are the Surefire and Failsafe
runs, and the Lab 40 OWASP Dependency-Check profile. The scan currently fails at
`failBuildOnCVSS 7` with 70 findings across 11 Spring Boot 3.3.5 managed
transitives, all triaged with owners and dates in
`examples/lab40-crm/docs/security-findings.csv`. A pipeline that runs it
unconditionally is red from its first run, so the threshold that is enforced has
to be a decision recorded in the runbook rather than a default.

## Step 3 — CRM identity

`CUS-1001` Amina Khan ACTIVE and `CUS-1002` Ravi Singh PROSPECT are synthetic
fixtures seeded by `FixtureLoader`. They may appear in test evidence and in
uploaded Surefire and Failsafe reports. Correlation id `lab-request-001` may
appear in the same places. Real customer data appears nowhere, and no dump is
ever attached to a run.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [x] File exists at `notes/lab43-pipeline-policy.md`
- [x] Three events covered
- [x] Verify vs package split clear
- [x] No secrets in policy
