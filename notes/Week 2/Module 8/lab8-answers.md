Lab 8 CRM skeleton (concepts, experiments, reflection)


STEP 3 LAYER TABLE

  concept        package              owns                              must not own
  presentation   controller           accept/return DTOs, map calls     SQL, business rules
  business       service              rules, orchestration              HTTP headers, JDBC details
  persistence    repository           save/find                         REST mapping
  domain         entity               customer fields                   request JSON shapes
  contracts      dto                  request/response                  persistence annotations
  cross-cutting  config, exception    wiring, failure types             happy-path create logic

Dependency direction: controller -> service -> repository -> entity, controller
and service also -> dto, entity imports nothing from the other layers.
Validation will live at the service with the controller boundary checking shape,
persistence lives behind the repository only.


CONCEPTS TO DISCUSS

1. The main data or request flow once create-customer is implemented

Client request with lab-request-001 hits CustomerController as a
CustomerRequest, service validates, assigns CUS-1001 and the ACTIVE default,
builds the Customer entity, repository saves it, a CustomerResponse goes back
out. Same sequence as Ex 5, nothing skips a layer, nothing calls back up.

2. The trust boundary and which layer will own input validation later

The controller is the trust boundary, everything arriving there is untrusted.
Shape checks happen at that edge, the business rules like blank name or
duplicate id belong to the service, so a future batch caller that skips HTTP
still can't get bad data past it. Same instinct as Account in Lab 7, validate
where the rule lives.

3. The success and failure contract for create customer

Success returns a CustomerResponse carrying the assigned id and status, caller
never sees the entity. Failure is a typed exception, CustomerNotFoundException
for lookups, validation failures later, thrown before anything is stored. Today
both paths throw UnsupportedOperationException, which is the Lab 8 contract
working as intended.

4. Stable identity versus display name

CUS-1001 is the identity, Amina Khan is data hanging off it. Names change,
collide, get corrected, the id never does, so every reference between systems
uses the id. The service assigns it on create, callers never pick their own.

5. Retry and idempotency implications at the repository boundary

A retried save after a timeout can create the same customer twice unless
something makes the operation idempotent, either save keyed on the stable id or
a dedupe check in the service. Reads retry safely, writes don't until that's
designed. 

6. Local development shortcut versus production design

In-memory list is fine for the next few labs, it vanishes on restart and that's
acceptable. Production needs PostgreSQL, transactions, and backups. The point of
the repository boundary is that swap happens behind findById and save without
the service noticing.

7. Logs, metrics, or UI evidence support will need once APIs exist

Every request logged with lab-request-001 style correlation id plus the customer
id, so one failing create can be followed across layers. Metrics on create/get
latency and error counts by exception type. The UI just needs the safe message,
the log keeps the detail, same split as the ATM.

8. Behavior with two application instances sharing the same customer IDs

Two instances with in-memory lists each hold their own truth, a customer created
on one doesn't exist on the other, and both could hand out CUS-1001. Shared ids
need a shared store, or at least id generation moved to something both instances
consult. Reason the in-memory phase can't outlive the demos.

9. Why entity must not import controller

That import points outward, the domain would depend on transport. A URL or
payload rename would break Customer, and the entity couldn't be reused off an
HTTP path. Ex 6 classified it problematic, and the practical check stays cheap,
read the imports under entity/, any controller import is a broken rule.

10. What belongs in dto vs entity for the same Amina Khan create request

CustomerRequest carries what the caller may send, name and email, no id and no
status. Customer carries the full domain record the service builds, id, status,
createdAt. CustomerResponse exposes the outcome, id and status included. Three
shapes for one customer, mapped in the service, so neither side leaks into the
other.


FAILURE EXPERIMENTS

1. renamed pom.xml -> mvn compile

  BUILD FAILURE, "The goal you specified requires a project to execute but
  there is no POM in this directory (...\examples\lab8-crm)". Maven refuses to
  even start without a POM. Renamed back, next build clean.

2. throwaway main calling new CustomerRepository().findById("CUS-1001")

  Exception in thread "main" java.lang.UnsupportedOperationException: Lab 8
  stub — implement later
    at com.northstar.crm.repository.CustomerRepository.findById(CustomerRepository.java:9)
    at com.northstar.crm.Throwaway.main(Throwaway.java:8)
  Loud failure at the exact line, which is the point of throwing instead of
  returning null. Deleted the harness after, stubs stay.

3. mvn clean compile twice

  BUILD SUCCESS both runs, clean wipes target and the second build recreates
  all nine class files identically. Build is repeatable, nothing depends on
  leftover state.

4. import com.northstar.crm.controller.CustomerController inside CustomerRepository

  Compiles, BUILD SUCCESS. The compiler has no opinion on layer direction,
  which is exactly why the rule is written in CODING-STANDARDS.md and checked
  by reading imports. A reviewer rejects it because persistence now depends on
  presentation, the cycle from Ex 6. Removed immediately, rebuilt clean.


SECURITY AND PRODUCTION REVIEW

1. Untrusted inputs: nothing today, later every API payload arriving at the
   controller, plus anything read off Kafka or the database once those exist.
2. Authn and authz land at the controller boundary later, validation splits,
   shape at the controller, business rules in the service.
3. Sensitive values: none in Lab 8 and application.properties is comments only,
   keeping it that way is the deliverable.
4. Retry safe: mvn compile, any read. Create customer isn't until idempotency
   is designed at the repository.
5. Partial failure: stubs throw before storing anything, so there's no partial
   state to clean up yet. Real bodies must keep that ordering, validate then
   mutate.
6. Operator monitors later: API latency, error rate by exception type, DB
   health. None of it exists yet, noting the gap is the answer.
7. Unacceptable local default: empty stubs, no auth, and later an in-memory
   store with no persistence. Fine for the lab, none survive contact with real
   data.
8. Contracts versioned later through the package split plus WSDL/OpenAPI in the
   API labs, DTOs are where versions will show up.


REFLECTION QUESTIONS

1. Which design decision most affected correctness of the skeleton?

Package folders matching com.northstar.crm exactly. Everything else is empty
shells, but one case mismatch or a file outside src/main/java and Maven
silently drops the class. The layout is the correctness.

2. Which failure was hardest to diagnose?

None bit this time, but experiment 4 is the dangerous one in real work, wrong
dependency direction compiles clean and reports nothing. Path and POM mistakes
announce themselves, a layer violation just sits there until review.

3. What evidence proves the layered structure is real, not only aspirational?

Nine class files under target\classes mirroring the seven packages, the compile
log, and imports that match the dependency table. Plus experiment 2, calling a
stub throws from the repository line, so the wiring controller -> service ->
repository actually resolves.

4. What breaks first at ten times the team size if packages are messy?

Ownership. Nobody can say which package a rule belongs to, so business logic
lands in controllers, every change fans out sideways, and merges collide
constantly. The dependency rule stops being checkable by reading imports.

5. Which concern should move to shared infrastructure later?

Logging with correlation ids, config, and exception-to-response mapping. Every
service the platform grows will want the same three, none of them are CRM
business logic.

6. What must change before real customer data is used?

Real persistence with transactions and backups, auth on the boundary,
validation actually implemented, PII kept out of logs, secrets out of
properties. The skeleton bans none of that today because nothing runs.

7. How does this lab connect to Labs 9-12 and later CRM platform pieces?

Lab 9 grows this same pom, 10-12 fill the stub bodies with domain and service
code, 13+ hangs SOAP contracts off the controller, 22+ drops Spring into
config. The packages are the fixed points, the files inside them change.

8. What metric, log field, query plan, or UI state matters most once APIs exist?

The correlation id on every log line. One failing create becomes traceable
across controller, service and repository without guessing. Latency and error
counts matter, but only after you can follow a single request.

9. Why keep DTOs separate from entities for creating Amina Khan?

CustomerRequest has no id and no status, the service assigns both. Fold the two
together and callers can send status ACTIVE and an id of their choosing, and
later JPA annotations end up on the API contract. The split is free while both
are empty.

10. When Spring Boot arrives, which packages stay stable vs which files change first?

entity, dto and exception barely move. config changes first, AppConfig becomes
real wiring, then the controller grows annotations and the repository swaps its
body for JPA. The service signatures hold, which is the payoff of the layout.


CHECKPOINT A - project root + maven layout

  1  pom.xml with com.northstar:customer-service:0.1.0-SNAPSHOT   Pass
  2  src/main/java, src/main/resources, src/test/java exist       Pass
  3  seven packages under com.northstar.crm                       Pass
  4  edited via IntelliJ workspace                                Pass

CHECKPOINT B - stubs compile and Main runs

  1  entity, DTOs, repository, service, controller, config, exception, Main   Pass
  2  mvn clean compile -> BUILD SUCCESS                                       Pass
  3  java -cp target\classes com.northstar.crm.Main prints banner + IDs       Pass
  4  no Spring/JPA/Kafka imports (grep found 0)                               Pass

CHECKPOINT C - documentation

  1  docs/layer-flow.md narrates CUS-1001 / lab-request-001       Pass
  2  docs/CODING-STANDARDS.md states hard layer rules             Pass
  3  project LAB-8-GUIDE.md explains compile/run                  Pass

CHECKPOINT D - failure evidence + security

  1  four failure experiments recorded above                      Pass
  2  layer-direction violation understood and reverted            Pass
  3  no secrets, target/ ignored, concepts answered               Pass


MANUAL VERIFICATION

  1   pwd is examples\lab8-crm                                            Pass
  2   mvn clean compile -> BUILD SUCCESS                                  Pass
  3   source listing shows all nine .java files                           Pass
  4   Main prints packages + CUS-1001 / CUS-1002                          Pass
  5   CODING-STANDARDS.md and layer-flow.md exist, name the layers        Pass
  6   grep springframework|jakarta.persistence|kafka over src -> 0 hits   Pass
  7   .gitignore has target/, git status shows no target files            Pass
  8   findById("CUS-1001") from harness -> UnsupportedOperationException  Pass
  9   second clean compile still BUILD SUCCESS                            Pass
  10  notes carry lab-request-001 and NOW vs FUTURE boundaries            Pass

Main output as run:

  Northstar CRM skeleton — Lab 8
  Packages: controller, service, repository, entity, dto, config, exception
  Examples: CUS-1001 Amina Khan ACTIVE | CUS-1002 Ravi Singh PROSPECT

Evidence: command outputs pasted above, screenshots in notes/screenshots/lab-8/.
