Northstar CRM build (Lab 25)

  mvn -B test
  mvn -B spring-boot:run

  curl -s -H "X-Correlation-Id: lab-request-001" \
    http://localhost:8080/api/customers/CUS-1001
  curl -s -H "X-Correlation-Id: lab-request-001" \
    http://localhost:8080/api/customers/CUS-1002
  curl -s -X POST http://localhost:8080/api/customers \
    -H "Content-Type: application/json" \
    -H "X-Correlation-Id: lab-request-001" \
    -d '{"id":"CUS-1003","name":"Maya Chen","email":"maya@example.com","status":"PROSPECT"}'
  mvn -q test -Dtest=CustomerServiceTest

Full path. The timed commands above plus the list route and the status change,
whose body binds through api/StatusUpdateRequest since the guide names
updateStatus and PATCH without fixing a payload shape:

  curl -s http://localhost:8080/api/customers
  curl -s -X PATCH http://localhost:8080/api/customers/CUS-1002/status \
    -H "Content-Type: application/json" \
    -H "X-Correlation-Id: lab-request-001" \
    -d '{"status":"ACTIVE"}'

Copied from the lab 25 starter, not carried forward from lab24-crm, so there is
no SOAP endpoint here. Controller to service to repository: CustomerController
calls CustomerService only, CustomerService holds the duplicate, not-found and
transition rules and talks to the CustomerRepository interface,
InMemoryCustomerRepository seeds CUS-1001 and CUS-1002 in its constructor and
owns the map.

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
| lab25-crm packages and runs | Pass, Started CrmApplication in 2.711 s on 8080 |
| Seeded GET works for CUS-1001 and CUS-1002 | Pass, 200 and 200 |
| Controller has no repository imports | Pass, 0 |
| Service has no HTTP types | Pass, 0 |
| CustomerServiceTest green | Pass, Tests run: 6 on two consecutive runs |
| AI review recorded | Pass, docs/lab25-001.md |
| Full path: GET /api/customers list | Pass, 200 with both seeds |
| Full path: PATCH activates CUS-1002 | Pass, PROSPECT to ACTIVE 200 |
| Full path: illegal transition rejected | Pass, 500 and CUS-1001 still ACTIVE |

TESTS

  mvn -B test    Tests run: 6

getSeededCus1001 and duplicateCreateRejected are the guide's pair, unchanged.
listReturnsSeedsAndCreated is the checkpoint C1 list() evidence, which the timed
path has no route for. activateRaviFromProspect,
illegalTransitionRejectedAndStatusUnchanged and closedIsTerminal cover the
transition table. Each builds its own repository, so a create or a status change
in one cannot change what the next one sees.

SECURITY NOTES

untrusted: the JSON body, the path id, the PATCH status value, and the
X-Correlation-Id header. The status value is checked against the transition
table but nothing validates the body shape. Bean Validation is lab 29.

authn/authz: none. All four routes are open, including PATCH, which changes
state. Lab 28 covers that.

sensitive: name and email. No customer data is logged in this build. The
rejection message carries the two statuses and the correlation id, no customer
fields. Emails are example.com only.

CLEANUP

  mvn -q clean
  git status

Ctrl+C spring-boot:run. target/ is ignored. Keep lab25-crm, lab 26 adds
profiles and config on this layering.

NOTES

Evidence and the failure experiments are in notes/screenshots/lab-25/.
Checkpoints and reflection answers are in notes/Week 3/Module 25/lab25-answers.md.
The AI review log is docs/lab25-001.md. Full GUIDE at
labs/Week 3 - Spring Framework and Enterprise Patterns/module-25/lab25/.
