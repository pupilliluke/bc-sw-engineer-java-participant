Northstar CRM build (Lab 12)

  mvn -B clean verify
  java -jar target\customer-service.jar

Walks the refactored API: create CUS-1001 and CUS-1002, get by id, a lookup
using a non-interned id that the frozen baseline would have missed, activate
CUS-1002 PROSPECT to ACTIVE, then the duplicate, blank and unknown failures with
their correlation id. Artifact is com.northstar:customer-service:0.1.0-SNAPSHOT,
packaged as target/customer-service.jar, same coordinates as Labs 9 to 11, the
project carries forward rather than forking.

TESTS

  mvn -B clean test

Eight tests, CustomerTest 2 and CustomerServiceTest 6. JUnit 5 is test scope
only, so it stays off the runtime classpath and out of the jar. Mockito is still
in the pom from Lab 11 and is unused here, the notifier it verified is not part
of the Lab 12 API.

CI

  mvn -B verify

Batch mode, non-interactive, stops after verification. Prefer it over install on
shared agents, install writes into that agent's ~/.m2 where every other job can
see it.

CLEANUP

  mvn clean
  git status

Keeps CustomerService.before.java.txt and everything under docs/. Nothing to
stop, no containers, no ports.

WHAT LAB 12 CHANGED

CustomerService started this lab as a deliberately poor class: one 39-line
doStuff method taking five Strings, a raw List, == on customer ids, null returned
for three different failures, four printlns, and an update path that fired when
the customer's name contained "UPDATE". The frozen copy is
src/main/java/com/northstar/crm/service/CustomerService.before.java.txt, 68
lines, not compiled because of the .txt suffix.

It now has three public methods, createCustomer, getCustomer and updateStatus,
over a Map<String, Customer> keyed by id, with three private helpers holding one
validation rule each. Failures throw, and every message carries
correlationId=lab-request-001. Ten smells, their fixes, both demo transcripts and
five failure experiments are in docs/before-after.md, the catalogue is in
docs/smells.md, and the standards self-check is docs/CODING-STANDARDS-check.md.

The Lab 8 DTO stubs create(CustomerRequest) and getById(String) are carried
through unchanged because CustomerController calls them. They still throw
UnsupportedOperationException, which is Lab 14's work.

SOLID APPLIED AND DEFERRED

Applied, SRP. doStuff did five jobs, validate, detect duplicates, map a status
string, build and store, then conditionally update. Those are now separate
methods, and the validation lives in named helpers rather than inline in a loop.

Deferred, DIP. No CustomerRepository interface behind the service. It adds two
types and a wiring decision to a lab whose deliverable is a before/after refactor
of one class, and Lab 8 already left a CustomerRepository stub in the tree, so
landing this properly is a reconciliation job rather than a greenfield one. The
guide names it as the next step too.

Deferred, ISP. There is no interface here to segregate, and the shape of the
first real client is decided by Lab 13's WSDL. Splitting ports now would be
designing against a caller that does not exist.

Full reasoning: notes/Week 2/Module 12/lab12-solid-scope.md.

ARCHITECTURE, NOW vs LATER

Now, a plain Java Maven service holding customers in an in-memory HashMap inside
one JVM. No framework, no network, no persistence. Restarting the jar loses every
customer, and two JVMs running this jar do not see each other's data at all, they
are two separate maps that happen to run the same code. Acceptable because this
lab is about the shape of the code, not about where the data lives.

Later, the same domain sits behind a Spring Boot API with PostgreSQL through JPA,
a React SPA over HTTPS and JSON, and Kafka carrying notification and audit
events. The map becomes a repository, the correlation id moves from a
concatenated string into MDC and then a trace header, and getCustomer's
IllegalArgumentException becomes a 404 with a problem-details body. The method
names were chosen to survive that move, which is the point of doing this before
Lab 13 rather than after.

SECURITY AND PRODUCTION REVIEW

1. Which inputs are untrusted?

Every argument to the three public methods. customerId, fullName, email and
phone all arrive from a caller and none is constrained by the type system beyond
being a String, so customerId and fullName are validated on entry. status is the
exception, it is a CustomerStatus, so the compiler already rejects anything
outside the four constants.

2. Where are authn/authz/validation enforced after refactor?

Validation is in requireNonBlank, requireUniqueId and requireExisting, called at
the top of each public method before anything is stored. Authentication and
authorisation are absent entirely, there is no caller identity in this project
and nothing here pretends otherwise, that arrives with the API labs.

3. Which values are sensitive, and where stored?

None beyond the samples. example.com addresses and 555-01xx phone numbers,
invented for the bootcamp. Email and phone sit on the Customer in memory and are
never logged, the exception messages carry the customerId and correlation id
only.

4. What can be retried safely?

getCustomer, freely, it does not mutate. updateStatus is idempotent for the same
target status, calling it twice with ACTIVE leaves ACTIVE. createCustomer is not
safe to retry blind, a second call throws IllegalStateException rather than
silently overwriting, which is deliberate, a retry that quietly replaced Amina's
record would be worse than a visible failure.

5. What happens after partial failure?

Every guard runs before customersById.put, so a rejected create leaves the map
untouched and there is no half-written record. The baseline had the same ordering
but reported failure by returning null, so a caller could not tell a rejected
write from a successful one without checking.

6. What would an operator monitor later?

The correlation id first, it is what ties a support report to one request, and
the rate of IllegalArgumentException out of getCustomer, which is the signal that
either callers hold stale ids or a lookup is broken. That second case is exactly
the bug this lab fixed, and in the baseline it was invisible because it returned
null like everything else.

7. Which local default is unacceptable in production?

Two. The in-memory HashMap, which loses everything on restart and is not shared
between instances. And the absence of a real logger, correlation currently rides
a concatenated string in an exception message rather than a structured log field.

8. How are contracts versioned later?

Lab 13 introduces a WSDL, and after that the method names in this class are no
longer free to change, they are the implementation behind a published contract.
Choosing createCustomer, getCustomer and updateStatus now rather than doStuff is
what makes that contract describable in the first place.

NOTES

docs/ai-review-notes.md is this lab, entry lab12-001 plus one manual refactor
decision. copilot-notes/ai-review-notes.md is Lab 10's review log, carried across
with the tree, nothing in it is Lab 12 work. Concepts and reflection answers are
in notes/Week 2/Module 12/lab12-answers.md. Full GUIDE at
labs/Week 2 - Backend, AI Tools and Testing/module-12/lab12/.
