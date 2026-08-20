# Lab 40 — Draft AppSec Go/No-Go Questions

## Step 1 — Questions

| # | Question | No-go if |
| - | --- | --- |
| 1 | is every High and Critical finding owned, with a date? | any sits at `needs_review`, or an accepted risk has no expiry |
| 2 | are there secrets in Git? | a password, token or signing key with a real value is committed |
| 3 | is there a negative authorization test? | the boundary is claimed but nothing proves it fails closed |
| 4 | is the suppression policy followed? | a suppression is missing CVE id, owner or expiry |
| 5 | does `mvn clean verify` still pass? | fewer tests pass than before the security work |

## Step 2 — Check the reference

three rules from leadership, and each one is a question above:

no ship on raw scanner volume. "412 findings" is not a status, it is an
unread report. question 1 asks for the shape of the triage, not the size
of it, and a scan with 400 Lows and every High owned ships while a scan
with three unclassified Highs does not.

no silent suppressions. question 4 is the whole of it. suppressing to go
green and deleting the profile to go green are the same act, so the
suppression file and the triage ledger get read together.

no secrets committed, ever. question 2 has one real answer against this
codebase today, and it is in section 3 below.

question 5 is mine rather than leadership's. it belongs because the
failure mode of a security gate is a fix that breaks the product, and
lab 39 ends at 22 green tests, which is the number to compare against.

the Predict prompt has one answer: a Critical CVE accepted with no expiry
is a no-go on question 1. accepted risk without a date is not accepted
risk, it is a finding that stopped being tracked, and the difference
between the two is exactly what a gate exists to catch.

## Step 3 — Tie to CRM

| # | Impact on agents serving Amina and Ravi |
| - | --- |
| 1 | an unowned High in a library on the request path is a hole in the endpoint agents use all day; ownership is what makes it somebody's Monday |
| 2 | the `jwt-secret` default `lab-only-change-me` is committed in `application.yml`, so anyone with the repo can mint an ADMIN token and read `CUS-1001` |
| 3 | nothing today stops an agent whose customer is Ravi from opening Amina; there is no object-level check and no test asking for one |
| 4 | a suppressed CVE in the JSON parser is one nobody re-reads, and that parser handles every request body an agent submits |
| 5 | a security change that breaks the status transition rules stops agents doing their job, which gets the gate switched off next release |

## Step 4 — Self mark

| Exercise | File | State |
| --- | --- | --- |
| 1 | `notes/lab40-owasp-surface-map.md` | filled, seven surfaces, top three ranked |
| 2 | `notes/lab40-dependency-check-plan.md` | filled, profile, command, suppression fields |
| 3 | `notes/lab40-triage-csv-sketch.md` | filled, headers and two synthetic rows |
| 4 | `notes/lab40-sast-todo-notes.md` | filled, read and write paths |
| 5 | `notes/lab40-assessment-outline.md` | filled, six sections and evidence index |

Overall prep: Pass

the honest qualifier on that Pass: it means the five notes files exist
and are filled from the real codebase rather than rubber-stamped. it does
not mean the CRM passes its own gate. against the five questions above,
today's answer on question 2 is a no-go and on question 3 is a no-go, and
both are findings in code I wrote in labs 36 and 39 rather than anything
a scanner surfaced. lab 40 is where they get fixed.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab40-gate-go-nogo.md`
- [ x ] Five questions drafted
- [ x ] Each has CRM impact
- [ x ] Secrets/suppression rules reflected
