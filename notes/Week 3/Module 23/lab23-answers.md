Lab 23 Spring Boot setup and auto-configuration (reflection questions, checkpoints)

built under examples\lab23-crm, copied from the lab 23 starter rather than from
lab21-crm. filled the two CustomerService TODOs, application.yml, and the two
profile teasers, then added CustomerControllerHttpTest for the step 8 full path.
2 surefire tests green on two consecutive mvn -B test runs. app captured on port
8080, health UP, CUS-1001 and CUS-1002 created with lab-request-001.


REFLECTION QUESTIONS

1. Which design decision most affected correctness?

leaving the missing-id path as the service IllegalArgumentException with no
@ControllerAdvice. it decides the whole failure contract: CUS-MISSING answers
500 and not 404, and the same throw is what turns a blank id into a 500 in
experiment 2. adding the advice would have changed both without touching the
service.

2. What evidence proves the implementation works?

02-boot-health-api-manual.txt has Started CrmApplication, health UP, info with
northstar-crm, POST 201 for both customers, GET 200 for both, CUS-MISSING 500,
and the two customer.created lines carrying correlationId=lab-request-001.
CustomerControllerHttpTest repeats the create and get on a random port and
01-mvn-test.txt shows it green twice at 2.

3. Which failure was hardest to diagnose?

experiment 1. i expected removing starter-web to fail at startup with no
embedded server, and instead nothing got that far, mvn clean compile stopped at
javac with package org.springframework.web.bind.annotation does not exist. the
first compile after the edit still passed because target/ held the old classes,
so it took the clean to see it.


CHECKPOINTS

| Checkpoint | Confirm | Result |
| --- | --- | --- |
| A1 | lab23-crm under examples/ | Pass, copied from starter/ |
| A2 | Boot parent + web + actuator + test | Pass, parent 3.3.5, java.version 21 |
| A3 | CrmApplication starts with embedded server | Pass, Tomcat started on port 8080 |
| B1 | application.yml sets name, port, Actuator | Pass, northstar-crm, 8080, include health,info |
| B2 | Create/get for CUS-1001 and CUS-1002 with lab-request-001 | Pass, 201 and 201, then 200 and 200 |
| B3 | Missing ID 500 (timed) or 404 with advice | Pass, 500, no @ControllerAdvice added |
| C1 | /actuator/health is UP | Pass, {"status":"UP"} |
| C2 | dev/prod profile teasers present and explained | Pass, dev DEBUG + show-details always, prod health only + never |
| C3 | Autoconfig vs ownership notes written | Pass, docs/autoconfig-notes.md, three and three |
| D1 | CrmApplicationTests green; full path also CustomerControllerHttpTest | Pass, Tests run: 2 on two runs |
| D2 | README runbook complete | Pass |
| D3 | No secrets / target/ committed | Pass, target/ in .gitignore, example.com emails only |

SECURITY AND PRODUCTION REVIEW

1. which inputs are untrusted?

the JSON body, the X-Correlation-Id header which is read into a log line and
never treated as identity, and every caller of /actuator.

2. where are authn/authz/validation enforced?

nowhere in this build. no security starter, no validation starter, no @Valid.
the only input rule is the blank-id check in CustomerService, and experiment 2
shows a blank name passing straight through to the store.

3. which values are sensitive?

name and email. the create log line carries id, status and correlation id only.
no api keys or db passwords in application.yml or either profile file, which is
Lab 26 work.

WINDOWS HOW-TO PASS CRITERIA

| # | Confirm | Result |
| - | --- | --- |
| 1 | workspace open in IntelliJ with SDK 21 | Pass, temurin-21.0.4 |
| 2 | lab project under examples/lab23-crm | Pass |
| 3 | GUIDE deliverables and checkpoints complete | Pass |
| 4 | commands succeed | Pass, mvn -B test and spring-boot:run |
| 5 | evidence under notes/screenshots/lab-23 | Pass, kept in the project as since lab 14 |
