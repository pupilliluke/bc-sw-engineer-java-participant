Northstar CRM build (Lab 41)

  mvn -B clean verify
  mvn -B spring-boot:run

spring-boot:run needs PostgreSQL up and CRM_APP_PASSWORD and JWT_SECRET set,
both from .env. Neither has a default, so a missing one stops startup rather
than falling back. Every /api/customers route needs a token; TOKEN below is the
accessToken from the login response.

  curl -s -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"agent1","password":"agent1"}'
  curl -s -H "Authorization: Bearer $TOKEN" \
    -H "X-Correlation-Id: lab-request-001" \
    http://localhost:8080/api/customers/CUS-1001
  curl -s -H "Authorization: Bearer $TOKEN" \
    -H "X-Correlation-Id: lab-request-001" \
    http://localhost:8080/api/customers/CUS-1002
  curl -s -X POST http://localhost:8080/api/customers \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -H "X-Correlation-Id: lab-request-001" \
    -d '{"id":"CUS-1003","name":"Maya Chen","email":"maya@example.com","status":"PROSPECT"}'
  mvn -q test -Dtest=CustomerServiceTest

Full path. The timed commands above plus the list route and the status change,
whose body binds through api/StatusUpdateRequest since the guide names
updateStatus and PATCH without fixing a payload shape:

  curl -s -H "Authorization: Bearer $TOKEN" \
    http://localhost:8080/api/customers
  curl -s -X PATCH http://localhost:8080/api/customers/CUS-1002/status \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -H "X-Correlation-Id: lab-request-001" \
    -d '{"status":"ACTIVE"}'

Copied from lab40-crm, which came from the lab 39 starter and not from
lab24-crm, so there is no SOAP endpoint here. Controller to service to
repository: CustomerController calls CustomerService only, CustomerService
holds the duplicate, not-found and transition rules and talks to the
CustomerRepository interface, which extends JpaRepository over CustomerEntity.
FixtureLoader seeds CUS-1001 and CUS-1002 at startup, idempotent by public_id,
so a restart does not duplicate them.

The gate checks: 0 repository imports in CustomerController, 0
springframework.web or springframework.http imports in CustomerService.

TRANSITIONS

| From | Allowed to | Rejected |
| --- | --- | --- |
| PROSPECT | ACTIVE, CLOSED | PROSPECT, SUSPENDED |
| ACTIVE | SUSPENDED, CLOSED | ACTIVE, PROSPECT |
| SUSPENDED | ACTIVE, CLOSED | PROSPECT, SUSPENDED |
| CLOSED | nothing | everything |

The table is lab 15's, carried forward. CLOSED is terminal and same-status is a
rejection. Anything not in the allowed column throws IllegalStateException
carrying the from, the to and the correlation id. Lab 15 held it in a
CustomerValidator over a CustomerStatus enum; this build has neither, the status
is a String on the model and the rule sits in CustomerService where the guide
puts transitions.

PASS CRITERIA

| Criterion | Result |
| --------- | ------ |
| lab41-crm packages and runs | Pass, mvn -B clean verify BUILD SUCCESS in 1:28 |
| Seeded GET works for CUS-1001 and CUS-1002 | Pass, 200 and 200 |
| Controller has no repository imports | Pass, 0 |
| Service has no HTTP types | Pass, 0 |
| CustomerServiceTest green | Pass, Tests run: 7 |
| Anonymous customer GET rejected | Pass, 401 |
| Full path: GET /api/customers list | Pass, 200 with both seeds |
| Full path: PATCH activates CUS-1002 | Pass, PROSPECT to ACTIVE 200 |
| Full path: illegal transition rejected | Pass, 409 and CUS-1002 still ACTIVE |

TESTS

  mvn -B clean verify    Tests run: 26

19 unit and 7 integration: CustomerServiceTest 7, SecurityRulesTest 8,
ProbeEndpointsTest 3, ForgedTokenSecurityTest 1, CustomerRepositoryIT 7.

getSeededCus1001 and duplicateCreateRejected are the guide's pair, unchanged.
emailIsNormalizedBeforeTheDuplicateCheck and missingCustomerIsNotFound came in
with the JPA move. activateRaviFromProspect,
illegalTransitionRejectedAndStatusUnchanged and closedIsTerminal cover the
transition table. Each test gets a fresh Mockito mock of CustomerRepository, so
a create or a status change in one cannot change what the next one sees.
CustomerRepositoryIT is the only test needing PostgreSQL, which is why the image
build runs package and the gate runs outside the image.

SECURITY NOTES

untrusted: the JSON body, the path id, the PATCH status value, the
Authorization header and the X-Correlation-Id header. The status value is
checked against the transition table, and CustomerService.validate checks name,
email shape and status on create and update. That validation is hand-rolled;
Bean Validation is lab 29.

authn/authz: JWT. POST /api/auth/login issues a token, every /api/customers
route requires ROLE_AGENT or ROLE_ADMIN, and /api/admin requires ROLE_ADMIN.
httpBasic and formLogin are disabled. Only /actuator/health and the readiness
and liveness probes answer anonymously.

sensitive: name and email. No customer data is logged in this build. The
rejection message carries the two statuses and the correlation id, no customer
fields. Emails are example.com only.

CLEANUP

  mvn -q clean
  git status

Ctrl+C spring-boot:run. target/ is ignored. Keep lab41-crm, lab 42 deploys
this image with Deployment, Service and probes.

NOTES

Evidence and the failure experiments are in notes/screenshots/lab-41/.
Checkpoints and reflection answers are in notes/Week 5/Module 41/lab41-answers.md.
The container runbook is ../docs/container-runbook.md. Full GUIDE at
labs/Week 5 - DevOps, CI-CD and OpenShift/module-41/lab41/.
