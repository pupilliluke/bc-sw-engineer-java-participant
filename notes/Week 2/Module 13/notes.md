Module 13: SOAP API Design with Java (exercise notes)

| # | Exercise | Where it lives | State |
| --- | --- | --- | --- |
| 1 | Fill fault envelope TODOs | lab13-fault-todos.md | Done |
| 2 | Operation matrix | lab13-operation-matrix.md | Done |
| 3 | Java to XSD map | lab13-java-xsd-map.md | Done |
| 4 | Contract-first mindset | lab13-contract-first.md | Done |
| 5 | Placeholder endpoint honesty | lab13-placeholder-honesty.md | Done |
| 6 | Lab 13 prep checklist | lab13-prep-checklist.md | Done |

every exercise names its own file so nothing lands here.
examples\module-13-exercises is the workspace the index names, empty so far, and
likely to stay that way, the module's artifacts are xml and markdown, the java
does not come back until lab 24.

module 12 ended on a refactored CustomerService with createCustomer, getCustomer
and updateStatus. module 13 does not touch java at all, it writes the contract
those three methods will have to satisfy when spring-ws hosts them in lab 24.


================================================================

Exercise 1: Fill Fault Envelope TODOs

filled in lab13-fault-todos.md. the six blanks, the envelope they build, and the
reason CUS-9999 rather than a real fixture carries the not-found example.

three things decided there and carried forward. the faultcode is soap:Client
because the slides give faultcode as Client or Server, so NotFound is not one of
them and the notfound half rides the detail as CUSTOMER_NOT_FOUND. the detail
carries customerId beside it because the exercise asks for it. and the
faultstring is lab 12's own message, requireExisting already builds "Customer not
found: " plus the id plus the correlation id, so the fault and the exception say
the same thing.

pass criteria

| # | Confirm | Notes |
| --- | --- | --- |
| 1 | All _____ replaced | Pass |
| 2 | CUS-9999 and lab-request-001 present | Pass |
| 3 | Lab 24 named | Pass |


================================================================

Exercise 2: Operation Matrix

filled in lab13-operation-matrix.md. GetCustomer and ActivateCustomer with in,
out and fault, plus the ravi happy path.

ActivateCustomer is not one of the lab's three operations, the lab overview
slide lists CreateCustomer, UpdateCustomer, GetCustomer and the evaluation
criteria name the same three. in lab 12 it is updateStatus with ACTIVE as the
argument, so on a contract it is UpdateCustomer carrying a status. a fourth
operation is a new portType entry partners have to regenerate against, an
optional field is not.

invalid transition is a fault I can document and cannot raise. the enum stops a
status that is not one of the four, nothing stops CLOSED going back to ACTIVE,
and lab 12's updateStatus has no transition check in it at all.

pass criteria

| # | Confirm | Notes |
| --- | --- | --- |
| 1 | File exists | Pass |
| 2 | Both operations have in, out and fault | Pass |
| 3 | Ravi activate path noted | Pass |
| 4 | Design-only boundary present | Pass |


================================================================

Exercise 3: Java to XSD Map

filled in lab13-java-xsd-map.md. lab 12's six Customer fields mapped to xsd
types, amina and ravi as the two row sets, plus the CUS-#### pattern.

optionality is read off lab 12 rather than guessed. createCustomer calls
requireNonBlank on customerId and fullName and nothing else, so email and phone
are minOccurs="0" in the map. status maps to an enumeration rather than a plain
string, the java enum has four constants and a string throws that away.

createdAt is the one I flagged instead of answering. LocalDateTime has no zone
or offset, so the value means 17:00 somewhere, and I have not used the xsd date
types yet. also LocalDateTime.now() differs every run, so samples need a fixed
timestamp.

the pattern stays documentation. lab 12 checks non-blank only, so enforcing
CUS-#### in the schema would make the contract stricter than the code. CUS-9999
fits it, which is why the not-found fault is a lookup failure and not a
validation failure.

pass criteria

| # | Confirm | Notes |
| --- | --- | --- |
| 1 | File exists | Pass |
| 2 | Table includes both customers | Pass |
| 3 | Id pattern proposed | Pass |
| 4 | Lab 24 hosting deferred | Pass |


================================================================

Exercise 4: Contract-First Mindset

filled in lab13-contract-first.md. the definition, two code-first risks, and the
two candidate operations.

both risks are argued off lab 12 rather than in the abstract. accidental
breaking changes, the doStuff to createCustomer/getCustomer/updateStatus rename
was a safe refactor because nothing outside the project called it, and the same
rename on a published wsdl is three operation renames and a breaking release.
framework leakage, the class still carries the lab 8 DTO stubs that throw
UnsupportedOperationException and an equals built on customerId, none of which
mean anything to a billing partner.

pass criteria

| # | Confirm | Notes |
| --- | --- | --- |
| 1 | File exists | Pass |
| 2 | Contract-first sentence written | Pass |
| 3 | Two code-first risks | Pass |
| 4 | Two operations listed | Pass |


================================================================

Exercise 5: Placeholder Endpoint Honesty

filled in lab13-placeholder-honesty.md. the definition, the three non-goals, and
what lab 24 adds.

the three non-goals are the exercise's own list, no Spring-WS @Endpoint, no Boot
app, no deploy to Tomcat. the classpath reason behind them is lab 12's own
comment, week 2 has no spring on the classpath.

pass criteria

| # | Confirm | Notes |
| --- | --- | --- |
| 1 | File exists | Pass |
| 2 | Placeholder defined | Pass |
| 3 | Three non-goals listed | Pass |
| 4 | Lab 24 referenced | Pass |


================================================================

Exercise 6: Lab 13 Prep Checklist

filled in lab13-prep-checklist.md. five of five notes present, fixtures verified,
self mark Pass.

CUS-9999 is recalled as the not-found fixture and deliberately kept out of the
valid customer table, amina and ravi stay the two that resolve.

examples\module-13-exercises does not exist. the index sets it up, the module
produced no java and no xml, so nothing ever needed to live there.

pass criteria

| # | Confirm | Notes |
| --- | --- | --- |
| 1 | File exists | Pass |
| 2 | Artifacts confirmed | Pass |
| 3 | CUS-9999 recalled | Pass |
| 4 | Design-before-hosting statement present | Pass |
