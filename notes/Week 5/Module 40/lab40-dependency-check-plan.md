# Lab 40 — Plan Dependency-Check Gate

## Step 1 — Profile sketch

a Maven profile `security-scan`, off by default so a normal `mvn verify`
does not wait on it, on when the gate runs.

```xml
<profile>
  <id>security-scan</id>
  <build><plugins><plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>PINNED</version>
    <configuration>
      <formats><format>HTML</format><format>JSON</format></formats>
      <failBuildOnCVSS>7</failBuildOnCVSS>
    </configuration>
  </plugin></plugins></build>
</profile>
```

- goal: `dependency-check:check`, the one that evaluates and can fail
- formats: HTML to read, JSON to diff and to feed the triage CSV
- `failBuildOnCVSS` 7, the High boundary, as the placeholder to argue
  about rather than a number to accept silently
- version pinned, not left to resolve

the version pin is the part the deck's sketch leaves out and the
exercise's Debug prompt asks about. an unpinned plugin drifts on its own
schedule, so the same commit scanned twice can produce different findings
and a build that passed last week fails today for reasons no diff
explains. pinning makes the scan a property of the commit.

7 is a threshold, not a policy. it fails the build on High and Critical
and lets Medium through, which is the right default only because
everything below it still lands in the triage ledger with an owner. a
gate that fails on everything gets switched off within a week.

## Step 2 — Check the reference

```
./mvnw -B -Psecurity-scan dependency-check:check
```

run from the CRM module root, `crm-api/`, since that is where the pom
with the dependencies lives. `-B` for batch mode so CI output is not
carrying progress bars. JDK 21 and the Maven Wrapper, matching lab 39's
toolchain, Temurin 21.0.4 and Maven 3.9.9.

the first run downloads the NVD data set and caches it locally, which is
minutes rather than seconds and is the answer to the Predict prompt. it
lands in the local Maven repository under
`~/.m2/repository/org/owasp/dependency-check-data/`, not in the project,
so it survives a `clean` and is shared across modules. CI needs that
directory cached or every pipeline run pays for it again.

an NVD API key raises the download rate limit. it is an environment
variable, never a pom value and never committed, the same rule
`CRM_DB_PASSWORD` follows in lab 39.

## Step 3 — Suppression policy draft

every suppression carries three fields:

| Field | Why |
| --- | --- |
| CVE id | what exactly is being suppressed, not a path glob that hides future findings too |
| owner | a person who answers for it, not "the team" |
| expiry date | the day it comes back if nobody acted |

a suppression without all three fails the gate. the failure mode this
prevents is the quiet one: someone suppresses a finding to get a release
out, the note explaining why lives in a chat message, and two years later
the entry is still there and nobody can say whether it was ever true.

suppression is for false positives and for time-bounded accepted risk. it
is not for making the build green, and deleting the profile to go green
is the same act with fewer steps. both show up in the triage ledger as an
absence, which is why the ledger and the suppression file have to be read
together.

## Step 4 — Folder prep

`notes/screenshots/lab-40/` for the sanitized HTML and JSON.

sanitized matters. the JSON report carries absolute file paths from this
machine, which is a username and a directory layout, and the HTML embeds
the same. those are trimmed before anything is committed.

nothing is scanned in the pre-lab. the plan is the deliverable here; the
run, the triage and the re-scan are lab 40's timed and full paths.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab40-dependency-check-plan.md`
- [ x ] Profile goal and report formats named
- [ x ] CVSS threshold placeholder present
- [ x ] Suppression fields include owner + expiry
