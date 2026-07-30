Lab 12 coding standards and refactoring (concepts to discuss, reflection
questions, checkpoints, manual verification)


CONCEPTS TO DISCUSS

1. Main data flow after refactor

createCustomer validates, checks the id is free, builds the Customer and puts it
in a Map keyed by customerId. getCustomer reads that Map by key. updateStatus
looks the customer up the same way and mutates status. One store, one key, three
entry points, nothing loops the list any more.

2. Trust boundary and where validation lives

Everything crossing a public method is untrusted, all four Strings come from a
caller. The boundary is the top of each public method, requireNonBlank,
requireUniqueId and requireExisting run before anything is stored. status is
already safe, CustomerStatus means the compiler did that check.

3. Success/failure contract

Success returns a Customer, never null. Duplicate id throws IllegalStateException,
unknown id and blank name or id throw IllegalArgumentException, and every message
carries correlationId=lab-request-001. The baseline returned null for all three,
so the caller could not tell them apart.

4. Stable identity vs mutable fields

customerId is the identity and never changes, it is the Map key and it is what
Customer.equals compares. status and email are mutable facts about that customer.
Amina renamed or activated is the same record; a different customerId is a
different customer even if every other field matches.

5. Retry and idempotency, create vs get

getCustomer is safe to retry, it does not mutate. createCustomer is not, the
second call throws rather than overwriting, which is deliberate, a silent upsert
would let a retry replace Amina's record with whatever the retry carried.
updateStatus is idempotent for the same target, twice ACTIVE leaves ACTIVE.

6. In-memory shortcut vs production persistence

A HashMap in one JVM is free, fast and honest about the lab's scope, and it
loses everything on restart. Production needs durability, transactions and
concurrent access from many instances, which is PostgreSQL behind a repository.
The Map is a stand-in for exactly that seam.

7. Logs and evidence for support

The correlation id, lab-request-001, is the thing that ties a customer's report
to one request in a log. Every failure message carries it, so a support ticket
becomes searchable. What the messages never carry is email or phone, the
customerId identifies the record without publishing anything about the person.

8. Two JVM instances mean independent memory

Two copies of the jar are two separate HashMaps that happen to run the same code.
Create CUS-1001 in one and the other has never heard of her. Nothing detects the
conflict, and if both later wrote to a shared database the last write would win
silently, which is why in-memory does not scale past one process.

9. Which SOLID ideas fit, which are deferred

SRP fits, it is method extraction inside one class and it is the lab. DIP and ISP
are deferred, a repository interface adds types to a lab about one class, and
there is no interface to segregate until Lab 13's WSDL says what the first real
client wants. Written up before the lab in lab12-solid-scope.md.

10. Why freezing a before snapshot matters

Without it, refactor and rewrite look identical when you are done, and only one
of them preserves behaviour. The frozen file is what lets anyone check the claim,
the "UPDATE" branch really was there, get really did use ==. A clean class with
no before is an assertion; with one it is evidence.


REFLECTION QUESTIONS

1. Which design decision most affected correctness

Keying a Map by customerId. It deleted the == bug rather than patching it, since
Map lookup compares values through hashCode and equals and there is no comparison
left to get wrong. The old list scan invited the bug back on every future method
that had to find a customer.

2. Which smell was hardest to justify removing

The "UPDATE" branch, because removing it is a behaviour change rather than a
tidy-up, and something might have relied on it. Experiment 5 settled it, two
customers differing only in name got different statuses from the same call. No
signature or document could describe that, so it went.

3. What evidence proves behaviour is preserved

Two demo transcripts run against the frozen baseline and the refactored class,
plus eight green tests and a table of every behaviour marked preserved, fixed or
removed. Blank, duplicate and defaulting all behave as before; the only
deliberate loss is the "UPDATE" branch and it is recorded as such.

4. What breaks first at ten times the length if smells return

Naming, and then everything. At 400 lines nobody reads doStuff top to bottom, so
the second responsibility hides, then the third. Bugs like the null returns stop
being findable by reading and start being found by customers, which is the state
the lab's business scenario opens in.

5. Which concern should move to shared infrastructure

Correlation and logging. Right now the id is a constant concatenated onto
exception messages by one class, and every future service would copy that. It
belongs in MDC behind a real logger, set once where the request enters, so no
method signature has to carry it.

6. What must change before real customer data

Persistence that survives restart, authentication and authorisation, which are
absent entirely, and encryption in transit and at rest. Also a logging review,
the discipline of keeping email and phone out of messages has to be enforced by
something better than my own care.

7. How this connects to Labs 8-11 and Lab 13

Lab 8 wrote the standards this class is now checked against, Lab 10 taught
reviewing AI output, Lab 11 built the test net that made this refactor safe to
attempt. Lab 13 publishes a contract over these method names, which is why they
were worth choosing carefully now rather than later.

8. Which metric or log field matters most after refactor

The correlation id, because it turns "Amina cannot be found" into one traceable
request. Second is the rate of IllegalArgumentException from getCustomer, which
is precisely the failure the baseline hid behind a null return and could not have
been alerted on at all.

9. Forward look, which deferred SOLID step comes next

The repository DIP. It is next because persistence is the next real change, and
an interface in front of the Map is what lets PostgreSQL arrive without the
service noticing. Not today because it would add types to a lab about one class,
and Lab 8's existing repository stub means it needs reconciling, not inventing.


CHECKPOINT A - baseline frozen

| # | Confirm | Notes |
| --- | --- | --- |
| 1 | lab12-crm under examples/ | Pass, copied from lab11-crm as the guide prefers |
| 2 | Messy CustomerService + CustomerService.before.java.txt | Pass, frozen at 68 lines, .txt so maven never compiles it |
| 3 | docs/smells.md has 8+ smells with CRM impact | Pass, ten, each with line numbers and an observed impact |

CHECKPOINT B - refactored API

| # | Confirm | Notes |
| --- | --- | --- |
| 1 | createCustomer / getCustomer / updateStatus present | Pass |
| 2 | No doStuff, no "UPDATE" magic branch | Pass, only doStuff hit in src is the frozen snapshot |
| 3 | Typed store, Map<String, Customer> preferred | Pass, customersById, no cast left in the class |
| 4 | equals used for IDs, exceptions replace null | Pass, Map keying does the equality, no method returns null |

CHECKPOINT C - tests and demos

| # | Confirm | Notes |
| --- | --- | --- |
| 1 | CustomerServiceTest green for create/get, duplicate, unknown | Pass, 6 tests, plus updateStatus, blank id and the non-interned lookup |
| 2 | Manual / Main demo for sample customers | Pass, both transcripts in docs/before-after.md |
| 3 | Correlation id in at least one failure/log path | Pass, all three failure paths, asserted in unknownCustomerFailsClearly |

CHECKPOINT D - evidence and standards

| # | Confirm | Notes |
| --- | --- | --- |
| 1 | docs/before-after.md complete | Pass, smell to fix map, method lists, both transcripts, sizing, experiments |
| 2 | AI review note or manual substitute | Pass, docs/ai-review-notes.md, lab12-001 plus one manual decision |
| 3 | Standards checklist done, mvn -B verify green | Pass, six rows, verify green at 8 tests |
| 4 | Failure experiments recorded | Pass, all five |


MANUAL VERIFICATION

| # | Check | Result |
| --- | --- | --- |
| 1 | Primary workflow succeeds for CUS-1001 / CUS-1002 | Pass |
| 2 | Invalid input rejected with exceptions, not null | Pass |
| 3 | Duplicate id fails clearly, IllegalStateException | Pass |
| 4 | Unknown id fails clearly with correlation context | Pass |
| 5 | No doStuff / raw List data / "UPDATE" magic remain | Pass |
| 6 | Before snapshot, before-after and smells docs exist | Pass |
| 7 | Restart clears in-memory data, understood and documented | Pass, README architecture section |
| 8 | Second JVM does not share memory, documented | Pass, concept 8 and README |
| 9 | No secrets in commits, target/ ignored | Pass, .gitignore carries target/ |
| 10 | mvn -B verify passes | Pass, 8 tests, jar built |

count note, eight tests is CustomerTest 2 plus CustomerServiceTest 6, which is
the guide's verified layout. lab 11's CustomerNotifierMockTest was removed with
the notifier, it verified an interface that is not part of the lab 12 API.
