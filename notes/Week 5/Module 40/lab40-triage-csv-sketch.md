# Lab 40 — Sketch Findings Triage CSV

## Reference

| Classification | Meaning |
| --- | --- |
| true_positive | confirm and fix, or accept with an owner |
| false_positive | document the CPE or path mismatch |
| accepted_risk | time-bounded and owned |
| fixed | re-scan evidence required |

## Step 1 — Columns

```
finding_id,source,cve_or_rule,cvss,dependency_or_location,path,classification,owner,due_date,notes
```

| Column | Carries |
| --- | --- |
| finding_id | `lab40-001`, stable across re-scans so the ledger can be diffed |
| source | `dependency-check` or `sast`, because the two find different kinds of thing |
| cve_or_rule | the CVE for SCA, a rule name for a manual SAST finding |
| cvss | blank for SAST findings, which have no CVSS |
| dependency_or_location | the artifact for SCA, the class and method for SAST |
| path | where it enters the build, direct or transitive |
| classification | one of the four above |
| owner | a person |
| due_date | a date, not "next sprint" |
| notes | the rationale, which is the whole value of the row |

`source` is the column the deck's header adds and the exercise's list
omits, and it earns its place: a manual authorization finding and a
transitive CVE need the same ledger but almost none of the same fields.
cvss is empty on SAST rows rather than invented, because a made-up score
would sort those rows against real ones.

## Step 2 — Check the reference

the four classifications are a decision, not a status. `needs_review` is
where a finding starts and is not a resting place; a row still saying
`needs_review` at the gate is an unanswered question, which is a no-go.

`accepted_risk` is the one with teeth. it requires owner and expiry, and
on the expiry date the row reopens rather than closing. `fixed` requires
re-scan evidence, meaning the finding is gone from a later report, not
that a commit claims to have fixed it.

## Step 3 — Sample rows

synthetic, invented for this sketch, not real CVEs from anything.

```csv
finding_id,source,cve_or_rule,cvss,dependency_or_location,path,classification,owner,due_date,notes
lab40-001,dependency-check,CVE-2026-00000,7.5,example-json:1.2.3,transitive via spring-boot-starter-web,true_positive,luke,2026-09-02,deserialization in a parser the API reaches through request bodies; fix is the patched 1.2.4
lab40-002,dependency-check,CVE-2026-11111,5.3,example-xml:2.0.1,transitive via a test-scope dependency,false_positive,luke,2026-08-26,test scope only, never on the runtime classpath; CPE matched the artifact name but the vulnerable code path is not shipped
```

the false positive row shows the shape the rationale has to take. "not
exploitable" is not a rationale. naming the scope, the classpath and why
the matched CPE is wrong is one, and it is what a reviewer checks rather
than takes on trust.

## Step 4 — CRM link

a true positive in the API layer is the one that reaches customers. the
request path an agent uses to open `CUS-1001` runs through
`CustomerController` to `CustomerService` to the repository, and every
library on that path handles data on behalf of an authenticated agent:
the JSON parser reads the request body, the JDBC driver carries the
query, the JWT library decides who the caller is. a deserialization flaw
in the first or a signature-verification flaw in the third is reachable
by anyone who can send the CRM a request.

not remediating any of that today. the point of the ledger at this stage
is that each finding has a classification, an owner and a date before
lab 41 puts this application in a container and moves it closer to
something deployed.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab40-triage-csv-sketch.md`
- [ x ] Headers match the triage model
- [ x ] Two sample rows classified
- [ x ] Accepted-risk rules stated
