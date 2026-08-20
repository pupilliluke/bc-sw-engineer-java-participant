# Lab 40 — Outline Security Assessment

## Step 1 — Sections

`docs/security-assessment.md`, six sections:

| # | Section | Holds |
| - | --- | --- |
| 1 | Scope and assets | what was assessed, what was not, and the fixtures used |
| 2 | Method and tool versions | Dependency-Check version, manual review, JDK and Maven |
| 3 | Findings summary | counts by severity and source, before and after |
| 4 | Remediations planned | one row per `lab40-00x` with what changed |
| 5 | Residual risks | what is knowingly shipping, owned and dated |
| 6 | Evidence index | claim to artifact path |

scope named as much by exclusion as inclusion. this assessment covers
`crm-api` and its dependencies. it does not cover the container image,
which does not exist until lab 41, the k3s manifests from lab 42, or the
React UI. saying so keeps a reader from assuming a clean report means
more than it does.

facts and assumptions stay separated inside sections 3 and 5. "the scan
reported zero High findings" is a fact with a report behind it. "the API
is not reachable from outside the lab network" is an assumption about an
environment that does not exist yet.

## Step 2 — Check the reference

every residual risk row carries five fields:

| Field | Rule |
| --- | --- |
| risk | one sentence, what could happen |
| severity | High, Medium or Low, with the reason if it disagrees with the CVSS |
| owner | a person |
| due date | a date |
| mitigating control | what reduces it in the meantime, or "none" |

`none` is an allowed value for mitigating control and is the most useful
one in the file, because a risk with no compensating control and a date
three months out is the row a reviewer should stop on.

severity is allowed to disagree with the scanner. a CVSS 7.5 in a
test-scope dependency is not a High for this application, and a manual
authorization finding with no CVSS at all can be the most severe thing in
the document. the reason goes in the row.

## Step 3 — Evidence index draft

| Claim | Artifact |
| --- | --- |
| dependencies scanned at a known version | `notes/screenshots/lab-40/dependency-check-report.html` |
| findings triaged with owners and dates | `notes/screenshots/lab-40/findings-triage.csv` |
| every suppression has CVE, owner, expiry | `crm-api/dependency-check-suppressions.xml` |
| authorization boundary holds | `SecurityRulesTest`, existing |
| object-level authorization holds | a test that does not exist yet |
| the application still works after changes | `mvn clean verify`, 22 tests green in lab 39 |
| no secrets committed | `.env` gitignored, `.env.example` with blank passwords |

placeholders where the artifact does not exist yet, named rather than
omitted. the object-level authorization row is the honest one: the claim
is listed, the evidence is missing, and that gap is what exercise 6's
go/no-go has to weigh.

the verify row is the control that catches security fixes breaking
behaviour. lab 39 ends green at 22 tests, so any number below that after
remediation is a regression rather than a security improvement.

## Step 4 — Scope honesty

pre-lab outline only. no scan has been run, no finding triaged, no
remediation made. section headings and the rules each section follows are
the deliverable here; the full remediation and re-scan cycle is lab 40's
timed and full path.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab40-assessment-outline.md`
- [ x ] Six sections present
- [ x ] Residual risk fields complete in template
- [ x ] Pre-lab scope stated
