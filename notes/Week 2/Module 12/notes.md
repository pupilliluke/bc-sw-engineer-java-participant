Module 12: Java Coding Standards and Best Practices (exercise notes)

| # | Exercise | Where it lives | State |
| --- | --- | --- | --- |
| 1 | Target API sketch | below | Done |
| 2 | SOLID apply vs defer | lab12-solid-scope.md | Done |
| 3 | Smell bingo | lab12-smell-bingo.md | Done |
| 4 | Equals vs == | below | Done |
| 5 | Fill correlation one-liner TODOs | lab12-correlation-todos.md | Done |
| 6 | Lab 12 prep checklist | below | Done |

no code artifact for 1 and 2, both are paper work. examples\module-12-exercises
is the workspace the index names, empty so far.

module 11 ended with the notifyCreated / notifyStatusChange split, where the
starter and the guide shipped two different names for one interface. module 12
opens with the same shape of conflict in exercise 1, so it gets checked up front
this time rather than discovered mid-lab.


Architecture note: in-memory NOW vs React/Kafka/PostgreSQL LATER
================================================================

Exercise 1: Target API Sketch

methods, as the exercise names them

    findById(String customerId)
    activateProspect(String customerId)
    validateStatus(...)          marked "maybe" in the exercise

name check against the lab, done before writing anything down. the lab 12
starter service is a deliberately messy class with two methods, doStuff(String,
String, String, String, String) and get(String). its closing TODO says

    // TODO: replace doStuff/get with createCustomer / getCustomer / updateStatus

and the guide agrees in four places, the verified layout table, the "what you
build" line, the starter test class, and checkpoint row 1, "createCustomer /
getCustomer / updateStatus present".

so the exercise and the lab name different APIs. same call as module 11, the
lab's names win, they are what the grader checks and what the starter test
already calls. the exercise names are kept below as the story, not the
signatures.

| Exercise name | Lab 12 name | Note |
| --- | --- | --- |
| findById | getCustomer(String customerId) | same method, lab name wins |
| activateProspect | updateStatus(String customerId, CustomerStatus newStatus) | activate is the story, updateStatus is the method |
| validateStatus | private validation helper | "maybe" in the exercise, stays private, see exercise 2 |
| — | createCustomer(String, String, String, String, CustomerStatus) | the exercise has no create at all, the lab needs one |

the sketch

    public Customer createCustomer(String customerId, String fullName,
                                   String email, String phone,
                                   CustomerStatus status)
    public Customer getCustomer(String customerId)
    public Customer updateStatus(String customerId, CustomerStatus newStatus)

    private void validateCustomerId(String customerId)

four things this sketch fixes on sight, all visible in the messy baseline.
doStuff takes five Strings including the status, so a caller can pass "AKTIVE"
and the chain of else-ifs quietly defaults it to PROSPECT. the sketch takes
CustomerStatus, so the compiler rejects the typo. doStuff returns Object,
get returns Object, both return null on failure, so every caller unwraps a cast
and a null check. the sketch returns Customer and throws. and doStuff both
creates and updates depending on whether the name contains "UPDATE", which is a
second method hiding inside the first, so updateStatus is split out.

the ravi path

activateProspect(CUS-1002) is PROSPECT to ACTIVE. through the sketch that is

    svc.updateStatus("CUS-1002", CustomerStatus.ACTIVE);
    svc.getCustomer("CUS-1002").getStatus()  ->  ACTIVE

same split as module 11 exercise 1, activate is the story and updateStatus is
the method, and the same fixture is doing the work. worth saying that in the
messy baseline the ravi path does not work at all, get uses == on the
customerId, so it only matches when the caller happens to hand back the very
same String object. that is exercise 4's material and it is the reason the
guide says to key a Map by id.

keep out

| Excluded | Why not now |
| --- | --- |
| SOAP endpoints, wsdl, jax-ws | lab 13, the index scope table names it |
| spring controllers, @RestController, @ControllerAdvice | no spring anywhere in week 2 |
| repository interface and an impl behind it | that is the DIP defer in exercise 2 |
| a notifier on this service | lab 11's collaborator, nothing in lab 12 asks for it |

the sketch is three public methods and one private helper on one class. the
temptation is to draw the ports now because lab 13 is visible from here, and
that is exactly the over-architecting the index warns about.

prep boundary

do not complete full lab 12 refactor in pre-lab.

pass criteria

| # | Confirm | Notes |
| --- | --- | --- |
| 1 | Methods listed | Pass |
| 2 | Ravi transition noted | Pass |
| 3 | SOAP/Spring excluded | Pass |


================================================================

Exercise 4: Equals vs ==

reference table, copied from the exercise

| Check | Use | Why |
| --- | --- | --- |
| status ACTIVE? | Objects.equals / enum | String identity is unsafe |
| same Customer instance? | == | Reference equality only |
| id CUS-1001? | equals | Value equality |

my version, null-safe status row added as step 1 asks

| Check | Use | Why |
| --- | --- | --- |
| status ACTIVE? | Objects.equals / enum | String identity is unsafe |
| same Customer instance? | == | Reference equality only |
| id CUS-1001? | equals | Value equality |
| status ACTIVE, either side possibly null? | CustomerStatus.ACTIVE == c.getStatus(), or Objects.equals(a, b) for Strings | enum == is already null-safe, it is false rather than a throw. Objects.equals is the String answer, "ACTIVE".equals(status) also works, literal first so a null status can't NPE |

bad

    if (status == "ACTIVE")          Fail

good, amina

    CustomerStatus.ACTIVE == amina.getStatus()

    // and for the id
    "CUS-1001".equals(amina.getCustomerId())

why the bad line is worse than just wrong. it compares references, so it should
always be false, except a literal in source is interned and every "ACTIVE"
literal in the program is the same object. so `String s = "ACTIVE"; s ==
"ACTIVE"` is true and the line passes a quick test. the same status read from a
file, a request, a socket, or built by concatenation at runtime is a different
object and the line goes false. it works on the desk and fails in production,
which is the worst failure mode available.

second reason it fails: `status == "ACTIVE"` on an enum-typed status doesn't
even compile, which is the point. the check only exists because the field is a
String, so the smell is upstream of the comparison.

jdk 21 note

prefer enums when the status set is closed, and northstar's is, four values,
PROSPECT ACTIVE SUSPENDED CLOSED. with an enum, == is the correct comparison,
not a compromise. the compiler rejects a typo like AKTIVE, the set is
exhaustive so a switch can be checked for completeness, and == on an enum can't
NPE the way status.equals("ACTIVE") does. a String status gets all three wrong.

where this already bites

lab 12's messy baseline has both halves of this exercise in it. line 55,
`x.getCustomerId() == id`, so getCustomer only matches when the caller hands
back the identical String object. line 15, `a == ""`, the same bug in the blank
check, and the starter's own comment flags the first and not the second.

lab 11's code is the counter-example and it is already mine. Customer.equals is
Objects.equals(customerId, other.customerId), value equality on the id, and
findByStatus filters with `status == c.getStatus()`, enum reference comparison,
correct because the constants are singletons. both rows of the table above, both
already in the tree.

boundary

pre-lab only, prepare for lab 12, do not complete the full refactor now.

pass criteria

| # | Confirm | Notes |
| --- | --- | --- |
| 1 | Table plus null-safe row | Pass |
| 2 | Bad and good snippets present | Pass |
| 3 | Enum preference noted | Pass |


================================================================

Exercise 6: Lab 12 Prep Checklist

artifacts, step 1 names four

| Artifact | Exercise | Where | There? |
| --- | --- | --- | --- |
| smell bingo | 3 | lab12-smell-bingo.md | yes |
| equals sheet | 4 | this file, above | yes |
| API sketch | 1 | this file, above | yes |
| correlation TODOs | 5 | lab12-correlation-todos.md | yes |

step 4 adds a fifth, the apply/defer SOLID note, lab12-solid-scope.md, present.
so all five exercises before this one have an artifact on disk, nothing is
carried only in my head.

fixtures

| customerId | fullName | status |
| --- | --- | --- |
| CUS-1001 | Amina Khan | ACTIVE |
| CUS-1002 | Ravi Singh | PROSPECT |

checked back against the module 10 exercise 2 sketch rather than trusted from
memory, both rows match. same check as module 11 ex 6 and for the same reason,
the status swap is the one that writes itself in unnoticed.

numbering note

step 4 reads "Pass if apply/defer SOLID note exists; else revisit exercise 5".
the apply/defer SOLID note is exercise 2, exercise 5 is the correlation TODOs.
read it as revisit exercise 2. same class of slip as the module 11 starter
labelling its entries 002-004 differently from the guide, noted here so the
checklist result isn't argued about later.

boundary

pre-lab only, prepare for lab; do not complete full lab 12.

self-mark

Pass. all four step 1 artifacts exist plus the SOLID note the step 4 gate
actually asks for, so there is nothing to revisit in 1 to 5.

three things carried into the lab. the target API is the lab's names, not the
exercise's, createCustomer / getCustomer / updateStatus, decided in exercise 1.
the two starred first fixes are == on Strings and the magic status strings,
from exercise 3. and correlation is a concatenated string this week, not a real
MDC, because there is no logging framework on the classpath, from exercise 5.

pass criteria

| # | Confirm | Notes |
| --- | --- | --- |
| 1 | Artifacts confirmed | Pass, four named plus the SOLID note |
| 2 | Fixtures correct | Pass, checked against module 10 ex 2 |
| 3 | Pre-lab-only statement present | Pass |




Reflection Questions
Write 3–6 sentence answers:

Which design decision most affected correctness?
Which smell was hardest to justify removing?
What evidence proves the refactor preserves intended behavior?
What breaks first at ten times method length if smells return?
Which concern should move to shared infrastructure (logging, IDs)?
What must change before real customer data is used?
How does this lab connect to Labs 8–11 standards and Lab 13 contracts?
What metric, log field, or support clue matters most after refactor?
(Forward look) Which deferred SOLID step (e.g. repository DIP) comes next—and why not today?