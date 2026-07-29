Lab 10 GitHub Copilot (concepts to discuss, reflection questions, manual
verification)


CONCEPTS TO DISCUSS

1. Inline completion vs a Copilot Chat request, when is each better

Inline continues the line I'm already typing, best for boilerplate the file
already implies, getters, the next branch of a pattern. Chat is better when
the rules have to be stated up front, like CustomerService with five methods
and named exceptions. This lab used ghost text as a sanity check and Chat for
the service.

2. Why prompt specificity changes enterprise output quality vs a vague comment

// customer class produced int id, a name field and an invented address,
a different business that still compiled. Naming customerId as a String
shaped like CUS-1001, the enum values and the reject rules got the right
class back. The model fills every gap I leave with the most common answer,
not mine.

3. The trust boundary between an AI suggestion and code touching customer data

A suggestion is untrusted input until a human has read it, same as a request
hitting the controller. The boundary is review, nothing generated reaches
src/ until it's walked line by line and logged in ai-review-notes.md.
save() and the one-shot dump both got stopped at that line, that's the
boundary working.

4. Which business rule protects integrity in Customer, enum vs free String

CustomerStatus as an enum means the compiler rejects a typo like AKTIVE and
there are exactly four states, a free String accepts anything and every
filter becomes a guess. findByStatus compares with == and can't miss. The
rule costs one small file and removes a whole class of bad data.

5. What happens if Copilot suggests a class or annotation not on the classpath

Best case the import fails to resolve and the build breaks loudly, jakarta
persistence here would do that since the pom has no JPA. Worst case it
compiles and quietly drags design the project banned, like @Entity implies a
database that doesn't exist. Rule from the pre-lab, reject any import I
can't name from JDK 21 or the pom.

6. Why review line by line instead of does it compile

updateStatus(id, null) compiled, ran and wrote null into a customer who then
vanished from every status query, found only by testing the method with bad
input. The synchronized blocks compiled too while only half-protecting the
list. The compiler checks syntax against the classpath, it has no opinion on
my business rules.

7. The risk of pasting real customer data or credentials into Chat

A prompt leaves my machine and gets logged somewhere I don't control, so
whatever's in it is published and delete doesn't unpublish. Experiment 3
drafted a fake SSN and even that is unsafe, it trips scanners and normalizes
the habit that eventually leaks a real one. Fixtures like CUS-1001 cost
nothing.

8. License and provenance risk on a large verbatim-looking block

A long block arriving fully formed with naming the project doesn't use is
the tell. Before accepting, search a distinctive line, find whether it
matches a real project and what its licence says. If it's genuinely someone
else's code, add the dependency properly or write my own, pasting it in
unattributed makes it the company's legal problem.

9. Why Copilot is not a runtime dependency of customer-service

Nothing in the pom references it, no import names it, and the jar runs on
any machine with Java 21 whether Copilot exists there or not. It's a
dev-time tool like the IDE. That's also why every acceptance needs review,
the code has to stand on its own once the assistant is gone.

10. How Lab 11 reuses this review discipline when generating tests

Generated tests get the same treatment as generated code, read every assert
before trusting green. A test that asserts the wrong fixture, Ravi as
ACTIVE, passes forever while guarding nothing. Same loop as this lab, scoped
prompt naming the fixtures and rules, line-by-line review, accept or reject
logged with a reason.


REFLECTION QUESTIONS

1. Which prompt changed most between first attempt and final accepted version?

The Customer entity one. // customer class came back with int id, a name field
and an invented address, no status and no createdAt. The accepted version
names all six fields with types, the CUS-1001 format, the four enum constants,
both constructors and equals/hashCode on customerId. Only the amount of spec I
typed changed between them.

2. Most dangerous Copilot suggestion, and how I caught it

updateStatus(id, null). It compiled, ran, and wrote null into a customer who
then vanished from every findByStatus query with nothing in the output saying
so. Found by calling the method with null, reading it looked fine. save() on
Customer was worse in one way, Copilot committed it without being asked, so
code I never accepted was already in history.

3. What evidence would convince a skeptical tech lead I did not blind-accept

ai-review-notes.md, specifically the rejections. The one-shot service layer was
rejected wholesale, save() was deleted after it compiled clean, and Copilot's
CustomerService took three edits before it went in with each one written down.
Harness output sits before and after those fixes, so the null-status bug is
there failing and then fixed. None of that can be written after the fact.

4. How review would change if this touched real customer PII

No real record goes into a prompt at all, fake-looking examples included.
updateStatus writing null is a lab annoyance here and a data integrity
incident on real records, so the validation stops being optional. Review needs
a second person rather than being self-certified, and logs need redaction.
This file becomes something a compliance team can ask for instead of lab
evidence.

5. Which task was faster with Copilot, which was slower once review counted

Faster was the first version of CustomerService and the accessors on Customer,
boilerplate with a spec in front of it. Slower was experiment 4, seven classes
and about twenty methods that took real time to read and put nothing into src.
deleteCustomer by hand took a couple of minutes because the guards, messages
and return were already decided by the methods above it. The saving shows up
in the first version of a class, not the fifth method.

6. How this lab connects to the Northstar CRM platform across Weeks 2-6

Lab 8 decided where code lives, Lab 9 made the build trustworthy, Lab 10 put
the first behaviour in. Lab 11 generates tests against this same
CustomerService, 12+ adds standards and APIs, the in-memory list becomes
CustomerRepository over JPA and PostgreSQL, and Spring arrives at 22. Most of
these classes get replaced on the way. The review rule is what carries, and it
applies to tests and config the same as to service code.

7. What I would put in .github/copilot-instructions.md to stop the JPA mistake

Java 21, plain Java only, no Spring and no JPA, with jakarta.persistence and
javax.persistence banned by name rather than by implication. Entity classes
are POJOs, persistence lives behind CustomerRepository, no save() on an
entity, per docs/CODING-STANDARDS.md. Import nothing that is not already in
pom.xml, fixtures are CUS-1001 and CUS-1002, and after experiment 1, never
commit. The file in .github right now is the Claude CLI wrapper doc, so this
is still to write.

8. Difference between "Copilot wrote this" and "I am responsible for this"

Where the text came from is not a defence in review or in an incident. The
name on the commit is who answers for it, and the assistant is not someone
anyone can ask. In practice it means not committing code I cannot explain with
the assistant closed, which is why the synchronized blocks came out. Copilot
put a Co-authored-by trailer on the commit it made in experiment 1, and that
moves credit without moving any of the responsibility.

9. How Lab 11 should treat AI-generated tests differently from production code

A wrong test passes rather than failing, so it guards nothing and says
nothing. Check every assert against the spec and the fixtures, not against the
implementation, since a test written from the code repeats whatever the code
got wrong. Then break the code and confirm the test goes red. No generated
test here would have caught updateStatus(id, null) because nobody asked for
it, so coverage of what was written is not coverage of what was required.


CHECKPOINT A - environment + copilot ready

| # | Confirm | Notes |
| --- | --- | --- |
| 1 | lab10-crm copied from Lab 9 under examples/ | PASS, after fixing a nested copy, project now at lab10-crm root |
| 2 | Copilot + Chat signed in, Check Status Ready | PASS |
| 3 | Sanity ghost-text suggestion observed in a .java file | PASS |

CHECKPOINT B - domain + service compile

| # | Confirm | Notes |
| --- | --- | --- |
| 1 | CustomerStatus, Customer, CustomerService under correct packages | PASS |
| 2 | No JPA/Spring annotations or imports in those files | PASS, grep over src found zero |
| 3 | mvn -q compile succeeds | PASS, BUILD SUCCESS |

CHECKPOINT C - behavior + sample IDs

| # | Confirm | Notes |
| --- | --- | --- |
| 1 | Main creates CUS-1001 ACTIVE and CUS-1002 PROSPECT | PASS |
| 2 | Status filter and updateStatus demonstrated | PASS, PROSPECT filter then activation to ACTIVE |
| 3 | Blank/duplicate/unknown ID rules exist in service code | PASS, all three throw, proven by harness |

CHECKPOINT D - review log + risks + experiments

| # | Confirm | Notes |
| --- | --- | --- |
| 1 | Entries lab10-001 to lab10-004 complete | PASS |
| 2 | At least one caught/corrected Copilot mistake documented | PASS, updateStatus null corruption and the save() reject |
| 3 | Failure experiments recorded, no secrets in prompts or git | PASS, four experiments, fixtures only |


MANUAL VERIFICATION

| # | Check | Result |
| --- | --- | --- |
| 1 | Copilot status Ready, workspace is lab10-crm | PASS |
| 2 | Customer / CustomerStatus compile with zero JPA/Spring imports | PASS |
| 3 | Service rejects blank ID, duplicate ID, unknown ID on update | PASS |
| 4 | Main prints both customers, PROSPECT list has CUS-1002, ACTIVE after activation | PASS |
| 5 | ai-review-notes.md has lab10-001 to lab10-004 | PASS |
| 6 | At least one deliberately caught Copilot mistake documented | PASS, save() experiment plus the null status bug |
| 7 | No real PII or secrets in prompts or committed files | PASS |
| 8 | git status shows no staged target/ or IDE junk | PASS, lab10-crm untracked, target/ ignored |
| 9 | mvn -q clean compile succeeds | PASS |
| 10 | Can explain accepted AI lines without reopening Chat | PASS, explanations written into the review log |

Main output as run:

  All customers: [Customer{customerId='CUS-1001', fullName='Amina Khan', status=ACTIVE}, Customer{customerId='CUS-1002', fullName='Ravi Singh', status=PROSPECT}]
  PROSPECT customers: [Customer{customerId='CUS-1002', fullName='Ravi Singh', status=PROSPECT}]
  After activation: Optional[Customer{customerId='CUS-1002', fullName='Ravi Singh', status=ACTIVE}]

Evidence: screenshot in notes/screenshots/lab-10/step9compile.png, full
review trail in examples/lab10-crm/copilot-notes/ai-review-notes.md.
