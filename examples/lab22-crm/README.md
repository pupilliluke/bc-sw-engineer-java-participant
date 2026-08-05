Northstar CRM build (Lab 22)

  mvn -B -Dtest=CustomerServiceTest test
  mvn -B -Dtest=CustomerServiceTest,CustomerServiceSpringTest test
  mvn -B test
  mvn -B spring-boot:run

  curl -H "X-Correlation-Id: lab-request-001" -H "Content-Type: application/json" \
    -d '{"id":"CUS-1001","name":"Amina Khan","email":"amina.khan@example.com","status":"ACTIVE"}' \
    http://localhost:8080/api/customers
  curl -s http://localhost:8080/api/customers/CUS-1001

Copied from lab21-crm per step 1 of the guide, artifact renamed to
com.northstar:lab22-crm:0.0.1-SNAPSHOT. Spring owns the object graph:
@Repository on InMemoryCustomerRepository, @Service on CustomerService and
NotificationService, constructor injection with final fields throughout, and
@PostConstruct / @PreDestroy on CustomerService. The graph is in
docs/dependency-graph.md.

Customer fields are id/name/email/status per step 2 of the guide, renamed from
lab 21's customerId/fullName. The JSON keys move with them, so the request
bodies in CustomerApiIT, CustomerLoggingIT, ActuatorIT and customers.html were
updated. The validation messages and the log reason codes still read customerId
and fullName; those are lab 20 contract in docs/logging.md and lab 22 does not
touch them.

TIMED-PATH PASS CRITERIA

| Criterion | Result |
| --------- | ----------- |
| App starts; no missing-bean errors | Pass |
| POST/GET CUS-1001 works (or unit/IT green) | Pass, GET 200, POST 409 duplicate under the lab 21 rule |
| Constructor DI + stereotypes present (no new of collaborators in service) | Pass |
| dependency-graph.md names CustomerService -> repository/notifier | Pass |

DEVIATIONS FROM THE STARTER

CustomerService keeps a third collaborator, CustomerMetrics, from lab 21. The
constructor is (CustomerRepository, CustomerMetrics, NotificationService), so
CustomerServiceTest builds it with a SimpleMeterRegistry rather than the
guide's two-argument form.

The guide's create(customer, correlationId) and get(id) are both present. get
throws IllegalArgumentException as the guide specifies; findById returning
Optional is kept alongside it because the controller answers 404 from it, which
is lab 19 behavior.

CUS-1001 and CUS-1002 are seeded by the repository constructor and the lab 21
duplicate rule rejects a repeat create, so the tests create CUS-2001 and
CUS-2202 instead. The guide's Amina and Ravi creates are recorded as 409s in
experiment 3.

SECURITY NOTES

untrusted: the JSON body, the form fields, the X-Correlation-Id header, and any
caller of /actuator.

authn/authz: none here, and DI does not add any. Validation stays at the
controller edge where lab 20 put it.

sensitive: fullName, email, phone, address, tokens. The notification log line
carries the customer id and the correlation id only.

Actuator exposure is unchanged from lab 21 and is lab-only; the beans endpoint
is not exposed.

CLEANUP

  mvn -B clean
  git status

Stop spring-boot:run and confirm the @PreDestroy line. target/ is ignored.
Keep lab22-crm, this bean graph is the base for the later Boot and JPA labs.

NOTES

Evidence and the five failure experiments are in notes/screenshots/lab-22/.
Checkpoints and reflection answers are in notes/Week 3/Module 22/lab22-answers.md.
The logging contract from lab 20 is docs/logging.md and the monitoring contract
from lab 21 is docs/monitoring-report.md. Full GUIDE at
labs/Week 3 - Spring Framework and Enterprise Patterns/module-22/lab22/.
