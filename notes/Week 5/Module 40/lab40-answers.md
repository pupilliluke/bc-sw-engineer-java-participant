Lab 40 application security testing on the CRM (reflection questions,
checkpoints)

built as examples\lab40-crm, a copy of lab39-crm with its own container
crm-postgres-lab40 and database crm40 so the scan and the fix could not
disturb lab 39's evidence. the work was a threat checklist and triage
ledger written before scanning, an OWASP Dependency-Check profile pinned
at 10.0.4, a manual data-flow pass, one vulnerability reproduced with a
failing test and fixed, and a security assessment with residual risks
owned and dated.

the scan found 128 findings across 11 dependencies, 70 at CVSS 7 or
above, and every one of them is a managed transitive of
spring-boot-starter-parent 3.3.5. the finding that actually mattered was
not in that list.


REFLECTION QUESTIONS

1. Which design decision most affected correctness of the security gate?

writing the reproducer before the fix. ForgedTokenSecurityTest failed
with "expected:<401> but was:<200>" and returned {"role":"ADMIN"} from
the admin endpoint, then passed unchanged after the fix. a test that has
never failed cannot be shown to detect anything, so the red run is what
makes the green run evidence rather than an assertion.

2. What evidence proves the remediation worked?

the same test file, unmodified, on both sides of the change: HTTP 200
with an admin body before, 401 after, with the diff confined to
application.yml line 34 and a value in a gitignored .env. beside it the
regression count, 22 tests green before the lab and 23 after, so nothing
was traded for the fix.

3. Which failure was hardest to triage, tool noise or real bug?

the noise, and specifically distinguishing it from signal. log4j-api
reported four CVEs at 7.5 and dependency:tree showed no log4j-core on the
classpath, so the vulnerable implementation is absent; angus-activation
was matched against cpe:2.3:a:eclipse:angus_mail, a different product
from the JAF activation jar actually present. both are defensible false
positives with a fact a reviewer can check, which is the standard "not
exploitable" fails to meet. the hardest part was that the real finding,
a signing key committed in application.yml, produced no scanner output at
all.


CHECKPOINTS

| Checkpoint | Confirm | Result |
| --- | --- | --- |
| A1 | lab40-crm baseline verify known | Pass, 22 tests green before any security work, recorded in 01-baseline-verify.txt |
| A2 | threat checklist + CSV headers | Pass, docs/threat-checklist.md with scope in and out, and the nine-column ledger header committed empty |
| A3 | Dependency-Check profile + pinned version | Pass, profile off by default, version pinned via ${dependency-check.version} = 10.0.4 |
| B1 | HTML/JSON reports generated | Pass, both written to reports/, gitignored for size and absolute paths |
| B2 | top findings classified with owners/expiry | Pass, 10 rows, 6 true_positive 3 false_positive 1 fixed, every row owned and dated |
| B3 | transitive path examined for at least one finding | Pass, five paths traced with dependency:tree, including tomcat-embed-core at 34 of the 70 findings |
| C1 | manual SAST notes for injection/authz/secrets | Pass, docs/sast-notes.md with file:line citations; injection traced and recorded as no finding |
| C2 | failing reproducer then fix | Pass, ForgedTokenSecurityTest red then green, test unchanged |
| C3 | re-scan + regression pass | Pass, after-scan identical since no dependency moved, verify green at 23 tests |
| D1 | security-assessment.md complete | Pass, scope, method, findings, severity rationale, remediation, residual risks, facts vs assumptions, evidence index |
| D2 | two consecutive test runs green | Pass, mvn clean verify green twice, 23 tests both times |
| D3 | no secrets / raw customer data in Git | Pass, .env gitignored, .env.example blank passwords, reports gitignored, fixtures synthetic throughout |
| D4 | peer walkthrough recorded | Fail, not performed, no peer available; the reproduce section of the assessment carries the commands a peer would run |
| D5 | residual risks have owners and due dates | Pass, six rows, each with owner, date and mitigating control or an explicit none |
| D6 | suppressions include expiry | Pass vacuously, zero suppressions used; the policy is in dependency-check-suppressions.xml and the file ships empty |


SCOPE HONESTY

the CRM does not pass its own gate. one finding is fixed and proven, six
remain open with owners and dates, and the build still fails at
failBuildOnCVSS 7 because Spring Boot 3.3.5 was never bumped. that is the
correct state to record rather than a green report that hides it, and the
parent bump is the next action with a date against it.

D4 is a genuine Fail rather than a self-awarded pass.
