Lab 25 service and repository layers (reflection questions, checkpoints)

built under examples\lab25-crm, copied from the lab 25 starter rather than
carried forward from lab24-crm, so this build has no SOAP endpoint. full path:
get, create, list and updateStatus, seeded InMemoryCustomerRepository, all rules
in CustomerService, list and PATCH routes added to the controller. 6 tests green
on two consecutive mvn -B test runs and on mvn -B clean test. app captured on
port 8080.


REFLECTION QUESTIONS

1. Which design decision most affected correctness (where rules live)?

putting the duplicate check and the transition table in CustomerService instead
of the controller or the map. ConcurrentHashMap.put overwrites without
complaining and setStatus takes any string, so nothing below the service would
have caught a repeat CUS-1003 or an ACTIVE to PROSPECT move, and nothing above
it would have been asked. experiments 2 and 4 are those two rules firing.

2. What evidence proves layering works?

two import counts and one compile error. CustomerController has 0 imports from
crm.repository and CustomerService has 0 from springframework.web or
springframework.http. experiment 5 put ResponseEntity in the service and the
build broke in the controller, which shows the seam is real and not just a
naming convention. the transition rule holding on both paths, PATCH and the unit
test, is the same rule reached two ways.

3. Which failure was hardest to diagnose?

experiment 2 from the caller's side. the illegal transition and the CUS-9999
not-found both come back as a bare 500 with no message, so from curl alone they
are the same failure. the from, the to and the correlation id only exist in the
server log. lab 29 is where that becomes a 409 with a body.


CHECKPOINTS

| Checkpoint | Confirm | Result |
| --- | --- | --- |
| A1 | lab25-crm under examples/ | Pass, copied from starter/ |
| A2 | Boot app packages successfully | Pass, mvn -q -DskipTests package |
| A3 | Packages for controller/service/repository present | Pass, api/, service/, repository/, model/ |
| B1 | CustomerRepository + seeded InMemoryCustomerRepository | Pass, CUS-1001 and CUS-1002 seeded in the constructor |
| B2 | CustomerService owns rules; no Web imports | Pass, 0 springframework.web or .http imports |
| B3 | Controller has zero repository imports; GET Amina/Ravi works | Pass, 0 imports, 200 and 200 |
| C1 | Create + service list() / unit evidence + duplicate rejection | Pass, CUS-1003 201, list route 200, duplicate 500 |
| C2 | CustomerServiceTest (getSeededCus1001, duplicateCreateRejected) green | Pass, both green, plus four more |
| C3 | AI review in docs/lab25-001.md or manual N/A | Pass, four accepts and five rejects with the JPA note |
| D1 | Two consecutive mvn test identical success | Pass, Tests run: 6 both times |
| D2 | README / layering notes complete | Pass, including the transition table |
| D3 | No secrets / target/ committed | Pass, target/ ignored, example.com emails, no customer data logged |

FULL PATH

| Item | Result |
| --- | --- |
| updateStatus in the service | Pass, lab 15 transition table, String keyed |
| PATCH /api/customers/{id}/status | Pass, CUS-1002 PROSPECT to ACTIVE 200 |
| GET /api/customers list route | Pass, 200 with both seeds |
| Illegal transition rejected | Pass, ACTIVE to PROSPECT 500, CUS-1001 still ACTIVE |
| CLOSED terminal | Pass, CLOSED to ACTIVE 500, CUS-1002 still CLOSED |
| Failure experiment 2 | Pass, was N/A on the timed path |

SECURITY AND PRODUCTION REVIEW

1. which inputs are untrusted?

the JSON body on POST, the path id on GET and PATCH, the status value in the
PATCH body, and the X-Correlation-Id header. the status value is the only one
checked, and only against the transition table. no @Valid anywhere, that is lab
29.

2. where are authn/authz/validation enforced?

nowhere yet. all four routes are open, including the PATCH that changes state.
lab 28 is auth and lab 29 is the validation and advice polish that also turns
the 500s into a 404 and a 409.

3. which values are sensitive?

name and email. nothing logs a customer in this build, so there is no PII dump
to redact. the rejection message carries the two statuses and the correlation
id and no customer fields. the seeds and CUS-1003 all use example.com.

WINDOWS HOW-TO PASS CRITERIA

| # | Confirm | Result |
| - | --- | --- |
| 1 | workspace open in IntelliJ with SDK 21 | Pass, temurin-21.0.4 |
| 2 | lab project under examples/lab25-crm | Pass |
| 3 | GUIDE deliverables and checkpoints complete | Pass, full path |
| 4 | commands succeed | Pass, mvn -B test and spring-boot:run |
| 5 | evidence under notes/screenshots/lab-25 | Pass, kept in the project as since lab 14 |
