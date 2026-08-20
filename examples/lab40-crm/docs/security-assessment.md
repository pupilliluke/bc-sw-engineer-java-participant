# Security Assessment — Lab 40, Northstar CRM

Release gate for `crm-api` before containerisation in Lab 41.

## Scope and assets

**In scope:** `crm-api` source, its `pom.xml` and every transitive dependency on
the runtime classpath, `application.yml`, and the Flyway migrations.

**Out of scope, and stated so a clean result is not read as more than it is:**
the container image (Lab 41 builds it, it does not exist yet), k3s manifests
(Lab 42), `crm-ui`, and the PostgreSQL server itself. No third-party system was
touched. Scan targets were this repository and the local `crm-postgres-lab40`
container only.

**Assets by sensitivity:**

| Class | Fields | Handling |
| --- | --- | --- |
| PII | `full_name`, `email`, `phone` | never in logs, reports or committed evidence |
| Identifiers | `public_id`, `customer_id` | safe in evidence |
| Business state | `status`, `balance`, `version` | safe in evidence |
| Secrets | DB password, JWT signing key | environment only, never in Git |

**Fixtures:** synthetic `CUS-1001` (Amina Khan, ACTIVE) and `CUS-1002` (Ravi
Singh, PROSPECT). No real customer data entered this lab at any point.

## Method and tool versions

| | |
| --- | --- |
| JDK | Temurin 21.0.4 |
| Maven | 3.9.9 |
| dependency-check-maven | 10.0.4, pinned via `${dependency-check.version}` |
| NVD API key | supplied from the environment as `${env.NVD_API_KEY}`, never in `pom.xml` |
| Database | PostgreSQL 17, `crm40`, Flyway at v2 |
| Date | 2026-08-20 |

```
mvn -B clean verify
mvn -B -Psecurity-scan dependency-check:check
```

The scan lives in a profile that is off by default, so an ordinary `verify`
does not wait on it. Confirmed: `mvn -B clean verify` runs 23 tests and no
analyzer.

**First NVD populate took ~52 minutes** (191 batches, 380,994 records); the
second run took **28 seconds**. The data store is `~/.m2/repository/org/owasp/
dependency-check-data/`, outside the project and surviving `clean`. A CI runner
without that directory cached pays the full 52 minutes on every build, which is
how a gate gets disabled — noted as a residual risk below.

**The Sonatype OSS Index analyzer was disabled** during both runs
(`Invalid credentials`). That is a separate optional service from NVD,
attempted anonymously and refused. NVD data was complete in both runs, so the
analysis stands; the red text in the log is not a failed scan.

Manual review supplemented the scanner: a data-flow pass from every untrusted
input to its sink, recorded in `docs/sast-notes.md` with `file:line` citations.

## Findings summary

48 dependencies scanned, 11 carrying CVEs, 128 findings in total.

| Severity | Count |
| --- | --- |
| Critical | 20 |
| High | 50 |
| Medium | 50 |
| Low | 8 |

70 findings score ≥ 7.0 and fail the gate at the configured threshold.

Ledger: `docs/security-findings.csv`, 10 rows — 8 from the scanner, 2 manual.
6 `true_positive`, 3 `false_positive`, 1 `fixed`. Every row has an owner and a
date.

### Severity rationale — where our judgement differs from the scanner

**70 is not 70 distinct defects.** `spring-boot` and `spring-boot-starter-web`
report the same 5 CVEs under the same CPE; likewise `spring-core`/`spring-web`
and the two `spring-security` artifacts. Counting artifacts rather than defects
roughly doubles the apparent total.

**Highest CVSS is not highest priority.** `spring-security` at 9.1 is dated a
week earlier than Tomcat at 9.8, because it sits on the authentication and
authorisation path that every role check in `SecurityConfig` depends on. A flaw
there undermines controls rather than one request.

**The most serious finding in this assessment has no CVSS at all.**
`lab40-010`, a committed JWT signing default, is not a library version and no
scanner would ever report it. It defeated every authorisation control in the
application simultaneously.

## Remediation

### lab40-010 — committed JWT signing key — FIXED, proven

`application.yml:34` shipped `jwt-secret: ${JWT_SECRET:lab-only-change-me}`.
The fallback after the colon is the signing key when the variable is unset, and
it is committed. Because HMAC-SHA256 is a public algorithm and `JwtService` is
an ordinary class, anyone with read access to the repository could mint a token
for any subject and any role.

**Reproducer written first, before any fix.** `ForgedTokenSecurityTest`
constructs its own signer with the leaked literal — deliberately not the
`@Autowired` bean, which would only prove the application trusts itself — mints
`("mallory", "ADMIN")`, and sends it to `/api/admin/ping`.

| | Before | After |
| --- | --- | --- |
| Result | `AssertionError: Status expected:<401> but was:<200>` | pass |
| Response body | `{"role":"ADMIN","ok":"true"}` | 401, no body |
| Handler reached | `AdminController#ping()` | none |

**Fix, smallest root cause:** delete the fallback so the property reads
`${JWT_SECRET}`, and supply a real value through `.env`, which is gitignored.
The application now refuses to start when the variable is absent —
`Could not resolve placeholder 'JWT_SECRET'` — which converts a silent
vulnerability into a loud misconfiguration. This is the pattern `CRM_APP_PASSWORD`
on line 14 already followed.

**The test file did not change between red and green.** That is what makes it
evidence: a test that has never failed cannot be shown to detect anything.

**Regression:** 22 tests before, 23 after, all green. No behaviour was traded
for the fix.

### Also corrected during remediation

`spring.config.import` resolved `../.env` against the JVM working directory, so
launching from a different directory loaded nothing — silently, because of the
`optional:` prefix. Now lists both candidate paths and is verified from both
`crm-api/` and `lab40-crm/`. A stale second `.env` inside `crm-api/`, read by
nothing, was deleted.

## Residual risks

| Risk | Severity | Owner | Due | Mitigating control |
| --- | --- | --- | --- | --- |
| Spring Boot 3.3.5 managed transitives carry 70 findings ≥ 7.0 (Tomcat, spring-core/web, spring-security, jackson-databind, postgresql) | High | luke | 2026-09-03 | none today; the gate fails the build, so this cannot ship unnoticed |
| `spring-security` 6.3.4 flaws sit on the authz path | High | luke | 2026-08-27 | role checks still enforced; no known exploit in this deployment |
| CI without a cached NVD data directory pays ~52 min per run | Medium | luke | 2026-09-10 | NVD API key reduces it to minutes; cache the directory in the pipeline |
| Copilot transmitted a typed secret to GitHub as editor context | Medium | luke | 2026-08-27 | secret rotated; repository content-exclusion for `.env` not yet configured |
| Config import is working-directory dependent and fails silently | Low | luke | 2026-08-27 | both paths now listed; secrets have no defaults, so a missed import crashes rather than running insecurely |
| PII in `DuplicateCustomerException` message reaches logs | Low | luke | 2026-09-03 | HTTP body is generic; only the log line carries the address |

**Remediation direction for row 1:** all 11 flagged artifacts are managed
transitives of `spring-boot-starter-parent` 3.3.5 — `postgresql` is a direct
dependency but its version still comes from the BOM. One parent version bump is
the candidate fix for all six `true_positive` rows, against 70 individual
suppressions. Which CVEs it actually clears is decided by the fixed-version
ranges in the HTML report and must be proven by an after-scan, not assumed.

**No suppressions were used.** `dependency-check-suppressions.xml` is present
and empty, carrying the policy in a comment: CVE id, owner and expiry are all
required, and a suppression missing any of the three fails the gate.

## Facts vs assumptions

**Facts** — each has an artifact behind it:

- 48 dependencies scanned, 11 vulnerable, 128 findings, 70 at ≥ 7.0
- `log4j-core` is absent from the classpath; `dependency:tree` shows only
  `log4j-to-slf4j → log4j-api`
- `angus-activation` was matched against `cpe:2.3:a:eclipse:angus_mail`, a
  different product
- No `@PreAuthorize`, `Principal` or `Authentication` appears anywhere in
  `src/main/java`
- The forged token returned HTTP 200 before the fix and 401 after
- 23 tests pass; before the lab, 22 passed

**Assumptions** — not verified here, and load-bearing:

- Every agent is entitled to every customer. The application has no ownership
  model, so `lab40-009` is classified `false_positive` under current policy
  rather than as a defect. **If that policy is wrong, this becomes the most
  serious finding in the document** and needs a schema change, an authorisation
  check and a test.
- The four `log4j-api` CVEs require `log4j-core`. Believed from the CPE match
  and the absent artifact; not confirmed against each CVE's individual
  description.
- No attacker has already used the committed default. The key existed in Git
  history and history was not rewritten, so a clone taken before rotation still
  contains it.

## Evidence index

| Claim | Artifact |
| --- | --- |
| baseline before any security work | `notes/screenshots/lab-40/01-baseline-verify.txt` |
| scan method, versions, before-state | `notes/screenshots/lab-40/02-scan-before.txt` |
| triage and transitive paths | `notes/screenshots/lab-40/03-triage-and-transitive-paths.txt` |
| failure experiments | `notes/screenshots/lab-40/04-failure-experiments.txt` |
| scope and OWASP mapping | `docs/threat-checklist.md` |
| manual findings with file:line | `docs/sast-notes.md` |
| triage ledger, owners and dates | `docs/security-findings.csv` |
| suppression policy | `crm-api/dependency-check-suppressions.xml` (empty by design) |
| reproducer, red then green | `crm-api/src/test/java/com/northstar/crm/security/ForgedTokenSecurityTest.java` |
| full scan output | `reports/dependency-check-report.{html,json}` (gitignored: bulky, and they embed absolute local paths) |

## Reproduce

```bash
docker compose up -d
mvn -B clean verify
mvn -B -Psecurity-scan dependency-check:check
mvn -B test -Dtest=ForgedTokenSecurityTest
```

Requires your own `.env` (see `.env.example`) and your own NVD API key in
`NVD_API_KEY` — neither is in this repository.
