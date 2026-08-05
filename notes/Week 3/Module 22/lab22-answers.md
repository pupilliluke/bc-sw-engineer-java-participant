Lab 22 Spring IoC and dependency injection (reflection questions, checkpoints)

built under examples\lab22-crm, copied from lab21-crm per step 1. the change is
stereotypes on the CRM components, constructor injection with final fields,
@PostConstruct and @PreDestroy on CustomerService, and docs/dependency-graph.md.
Customer fields renamed to id/name per step 2, which moved the JSON keys in
three IT classes and in customers.html. 2 surefire and 11 failsafe tests green
on three consecutive mvn -B clean verify runs. app captured on port 8080.


REFLECTION QUESTIONS

1. Which design decision most affected correctness (constructor vs field
injection)?

constructor injection. the fields are final, so the compiler rejects a
CustomerService that is missing a collaborator, and experiment 1 shows the same
failure moves to context refresh rather than to the first request. field
@Autowired cannot assign a final field and would have deferred both checks.

2. What evidence proves the graph works (unit + IT + curls)?

CustomerServiceTest builds the service with new and no container, which only
compiles because the constructor takes everything.
CustomerServiceSpringTest autowires the real graph and reads seeded CUS-1001.
02-api-and-lifecycle-manual.txt has POST CUS-2001 201, GET CUS-1001 200, and
the notification line with correlationId=lab-request-001.

3. Which failure was hardest to diagnose?

experiment 5, the new inside the service. the suite stayed green at 2 and 11,
startup was clean and the constructor parameter was still satisfied, so nothing
reported it. it took a probe that saved through the injected bean and read
through the service to see the two stores.


CHECKPOINTS

| Checkpoint | Confirm | Result |
| --- | --- | --- |
| A1 | lab22-crm under examples/ | Pass, copied from lab21-crm, artifact renamed |
| A2 | CrmApplication starts successfully | Pass, Started CrmApplication in 4.649 s |
| A3 | Domain Customer free of unnecessary Spring annotations | Pass, plain JavaBean, id/name/email/status |
| B1 | @Repository / @Service (and controller) stereotypes present | Pass, @Repository, two @Service, @RestController, two @Component |
| B2 | CustomerService constructor injection with final fields | Pass, three final fields, one constructor |
| B3 | No new of Spring-managed collaborators inside the service | Pass, experiment 5 measures the alternative |
| C1 | @PostConstruct / @PreDestroy evidence | Pass, 02-api-and-lifecycle-manual.txt, one line each |
| C2 | Pure unit test without Spring (CustomerServiceTest) | Pass, Tests run: 1 |
| C3 | @SpringBootTest IT (CustomerServiceSpringTest) | Pass, Tests run: 2 with both |
| D1 | docs/dependency-graph.md matches reality | Pass, includes CustomerMetrics as the third parameter |
| D2 | Correlation + fixture IDs documented | Pass, lab-request-001, CUS-1001, CUS-1002 |
| D3 | No secrets; lab-only beans endpoint (if used) not sold as prod | Pass, beans endpoint not exposed, actuator set unchanged from lab 21 |

SECURITY AND PRODUCTION REVIEW

1. which browser, network, or API inputs are untrusted?

the JSON body, the form fields on customers.html, the X-Correlation-Id header
which is echoed and never treated as identity, and every caller of /actuator.

2. where are authn/authz/validation enforced (DI does not replace them)?

validation is at the controller edge where lab 20 put it, and experiment 2
shows the blank name never reaching the service. there is no authn or authz in
this build. DI moved who constructs what and changed neither.

3. which values are sensitive in notification/lifecycle logs?

fullName, email, phone, address, tokens. notifyCreated logs the customer id and
the correlation id only, and the two lifecycle lines carry no customer data at
all.

WINDOWS HOW-TO PASS CRITERIA

| # | Confirm | Result |
| - | --- | --- |
| 1 | workspace open in IntelliJ with SDK 21 | Pass, temurin-21 |
| 2 | lab project under examples/lab22-crm | Pass |
| 3 | GUIDE deliverables and checkpoints complete | Pass |
| 4 | commands succeed | Pass |
| 5 | evidence under notes/screenshots/lab-22 | Pass, kept in the project as since lab 14 |
