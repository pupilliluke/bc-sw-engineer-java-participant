Coverage notes (Lab 17)

THE GATE

JaCoCo 0.8.12, three executions in the pom.

| Execution | Phase | What it does |
| --- | --- | --- |
| prepare-agent | initialize | sets argLine so the agent is on the surefire JVM |
| report | test | writes target/site/jacoco, HTML and CSV |
| check-service-package | verify | PACKAGE rule, com.northstar.crm.service, LINE COVEREDRATIO >= 0.80 |

The minimum is the property service.line.minimum so an experiment can override
it on the command line without editing the pom.

  mvn -B clean test     compiles, runs the suite, writes the report
  mvn -B clean verify   the same plus the check that can fail the build

clean matters. Without it the agent data from an earlier run is reused and the
ratio describes a suite that no longer exists.

WHAT IS MEASURED

The service package only, DefaultCustomerService and CustomerValidator. Those
two hold the transition table, the uniqueness rules and the not-found decision,
which is what the merge gate is about. Gating the whole project would raise the
number with entity getters and the Main demo transcript and say nothing about
the rules.

WHERE IT LANDS

Service package 46 of 46 lines and 20 of 20 branches after the last test was
added. Whole project 268 of 311 lines. Per-package figures and the uncovered
list are in notes/screenshots/lab-17/02-jacoco-service-coverage.txt.

THE BRANCH THAT CLOSED LAST

CustomerValidator line 56, the fullName guard. The HTML report read "1 of 4
branches missed" there: the blank case had a test and the null case did not.
aMissingFullNameIsA400LikeABlankOne in CustomerValidatorTest closed it.

Lines were already at 46 of 46 when that branch was still red, so the gate was
green before the branch was covered. A LINE gate cannot see a half-covered
condition on a line that ran; only the report shows it.

WHAT THE GATE DOES NOT PROVE

Run the suite without the three service test classes and the service package
still reads 0.84, because CustomerApiFacadeTest exercises the service through
the facade. The 0.80 line gate would pass with no service test written at all.
The gate is a floor against deletion, not evidence that the rules are asserted.

The evidence that the rules are asserted is the red-green run in
copilot-notes/ai-junit-review.md: a one-line reordering in changeStatus turns
six tests red with messages that name the defect.

REPRODUCING THE FAILURE

  mvn -B clean verify "-Dtest=!CustomerServiceTests,!CustomerValidatorTest,!CustomerValidatorParameterizedTest" -Dservice.line.minimum=0.90

  Rule violated for package com.northstar.crm.service:
  lines covered ratio is 0.84, but expected minimum is 0.90

The guide's version of this experiment raises the minimum to 0.99 on the full
suite. That passes here, because the package is at 1.00. Transcript in
notes/screenshots/lab-17/03-gate-fail-and-restore.txt.

WHAT IS NOT COMMITTED

target/, including target/site/jacoco. The .gitignore from lab 16 already
covers it. The numbers above and the excerpts under notes/screenshots/lab-17
are what stays in the repository.
