Module 10: GitHub Copilot Fundamentals (exercise notes)

| # | Exercise | Where it lives | State |
| --- | --- | --- | --- |
| 1 | Weak vs strong prompts | lab10-prelab-prompts.md | Done |
| 2 | Customer sketch for Amina | below | Done |
| 3 | Phantom annotation hunt | below | Done |
| 4 | Fill review-log TODOs | lab10-review-log-todos.md | Done |
| 5 | JDK 21 / Maven habit | below | Done |
| 6 | Lab 10 prep checklist | below | Done |

code artifact for ex 1 is examples\module-10-exercises\Customer.java.


================================================================

Exercise 2: Customer Sketch for Amina

fields

sketched by hand before asking copilot for anything, so there's something to
check the suggestion against.

| customerId | fullName | status |
| --- | --- | --- |
| CUS-1001 | Amina Khan | ACTIVE |
| CUS-1002 | Ravi Singh | PROSPECT |

same shape both rows, three strings, no extra fields. two rows on purpose,
one row lets a suggestion hard-code ACTIVE and still look right.

correlation

lab-request-001 is a request id, it belongs in logs and headers later, not on
Customer. one customer can appear in many requests, storing the last one on the
record is just wrong data. same split as lab 8, the correlation id rides the log
line, the entity holds domain fields.

boundary

sketch only, lab 10's full ai generation path is not started.

pass criteria

| # | Confirm | Notes |
| --- | --- | --- |
| 1 | Amina and Ravi rows correct | Pass |
| 2 | Correlation not stored as a Customer field | Pass |
| 3 | Explicit pre-lab boundary written | Pass |


================================================================

Exercise 3: Phantom Annotation Hunt

table, copied from the exercise with my trap row added last

| Seen in suggestion | Likely real? | Prep action |
| --- | --- | --- |
| @Entity / @Table | JPA only | Defer — not Lab 10 scope |
| @Service / @Autowired | Spring | Defer — hosting labs later |
| @NotNull (Jakarta) | Validation lib | Name it; don't invent imports |
| public record Customer(...) | Java 16+ | OK on JDK 21 |
| @NorthstarEntity | Invented (trap) | Reject — no such library |

trap row is the one to watch. it reads like a house annotation and there is no
import that resolves it, which is exactly how a hallucinated dependency gets
accepted. tell is that i can't name where it comes from.

reject rule

reject any import i cannot name from JDK 21 or an agreed maven dependency.

fixture check, ran against the suggestion in Customer.java

| Check | Found | Result |
| --- | --- | --- |
| Annotations or imports outside the JDK | none | Pass |
| CUS-1001 Amina hard-coded as | ACTIVE | Pass |
| CUS-1002 Ravi hard-coded as | not present | Pass, nothing to fail |

no ravi row in the generated file so the wrong-status trap didn't fire this
time, the rule stands for lab 10 when the second fixture goes in. a suggestion
that hard-codes CUS-1002 as ACTIVE is a review fail, ravi is PROSPECT and the
code compiles either way so only the sketch catches it.

out of scope

soap and spring boot hosting, those are labs 13 and 24.

pass criteria

| # | Confirm | Notes |
| --- | --- | --- |
| 1 | Table copied with one trap row | Pass |
| 2 | Reject-unknown-import rule written | Pass |
| 3 | Ravi status PROSPECT called out | Pass |


================================================================

Exercise 5: JDK 21 / Maven Habit

commands, run before any lab 10 coding

| # | Command | Want to see | Ran today |
| --- | --- | --- | --- |
| 1 | java -version | 21.x | openjdk 21.0.4 Temurin |
| 2 | mvn -version | Java version 21, runtime under the 21 jdk | maven 3.9.9, java 21.0.4 Adoptium |
| 3 | echo %JAVA_HOME% | the 21 jdk folder | C:\Program Files\Eclipse Adoptium\jdk-21.0.4.7-hotspot |

two commands, not one. java -version reads whatever is first on PATH, mvn
-version reports the jdk maven actually builds with, they can disagree and the
second one is the one that compiles.

path trap

not hypothetical on this machine. PATH carries both the adoptium 21 bin and
C:\Program Files\Common Files\Oracle\Java\javapath, and that oracle shim points
at java 22:

| Scope | PATH entry | Resolves to |
| --- | --- | --- |
| machine | C:\Program Files\Eclipse Adoptium\jdk-21.0.4.7-hotspot\bin | 21.0.4 |
| machine | %JAVA_HOME%\bin | 21.0.4, same jdk |
| user | C:\Program Files\Common Files\Oracle\Java\javapath | 22.0.2 |
| user | C:\Program Files\Eclipse Adoptium\jdk-21.0.4.7-hotspot\bin | 21.0.4 |

windows builds the process PATH as machine entries first then user entries
appended, so the adoptium bin at machine position 2 wins and the oracle shim
never gets reached. checked with Get-Command java, adoptium 21.

the trap is dormant, not active. it only fires if the machine entry stops
resolving, a temurin upgrade or uninstall, or oracle repairing javapath into the
machine PATH. then javac 22 compiles class file version 66 and java 21 refuses
it with UnsupportedClassVersionError. maven is pinned separately by JAVA_HOME so
mvn would stay on 21 either way, which is what makes the two commands worth
running as a pair.

fix habit: check where java resolves from, not only its version. `where java`
in cmd, or Get-Command java in powershell, first line should be the adoptium 21
bin. if javapath is first, move the 21 bin above it in PATH and open a new
terminal, PATH edits don't reach shells that are already open.

workspace

| Item | Path |
| --- | --- |
| Bootcamp workspace | C:\Users\lukel\java-bootcamp |
| Module 10 exercise code | examples\module-10-exercises |
| Module 10 prep notes | notes\Week 2\Module 10 |

exercise 1 code ran as a single file with java Customer.java, no pom in that
folder yet, so today mvn only matters as a version check.

out of scope

do not run the full lab maven goals until the timed lab 10 session.

pass criteria

| # | Confirm | Notes |
| --- | --- | --- |
| 1 | java and mvn checks listed | Pass |
| 2 | One PATH trap named | Pass, oracle javapath java 22 ahead of adoptium 21 |
| 3 | No-full-lab note present | Pass |


================================================================

Exercise 6: Lab 10 Prep Checklist

three things the timed lab asks for, skimmed off the lab 10 header

| # | Lab 10 asks | What prep covers it |
| --- | --- | --- |
| 1 | Weak vs strong prompting practice, logged as lab10-001 and lab10-002 | ex 1, both prompts already written |
| 2 | copilot-notes\ai-review-notes.md, entries lab10-001 to lab10-004, human review of every accepted hunk | ex 4 review log, ex 3 reject rule |
| 3 | Generated Customer entity, CustomerStatus enum, CustomerService and a Main harness printing CUS-1001 and CUS-1002 | ex 2 sketch, ex 1 Customer.java |

lab copies examples\lab9-crm to lab10-crm and builds with mvn clean compile,
still plain java and maven, no spring in week 2.

fixtures from memory

| customerId | fullName | status |
| --- | --- | --- |
| CUS-1001 | Amina Khan | ACTIVE |
| CUS-1002 | Ravi Singh | PROSPECT |

correlation id lab-request-001, logs and headers only, never a customer field.
the lab's own review entries are numbered lab10-001 to lab10-004, different
scheme, don't mix the two.

field name settled

lab 10's verified output prints Customer{customerId='CUS-1001', ...}, so
customerId is the name, the ex 2 sketch was right and the id in the ex 1 prompt
was the loose one. lab 10 also makes status a CustomerStatus enum, not a string,
so the prep record is deliberately simpler than the lab deliverable.

boundary

pre-lab only, prepare for lab, do not complete full lab 10.

readiness pass/fail

| # | Have it? | Where | Result |
| --- | --- | --- | --- |
| 1 | Weak and strong prompts | lab10-prelab-prompts.md | Pass |
| 2 | Phantom annotation checklist | ex 3 above | Pass |
| 3 | Review-log todos filled | lab10-review-log-todos.md | Pass |
| 4 | Toolchain checked | ex 5 above | Pass |

all four present so no revisit of exercises 1-4 needed. one thing to carry in,
the oracle javapath java 22 on PATH from ex 5, check where java resolves before
the timed session starts.

pass criteria

| # | Confirm | Notes |
| --- | --- | --- |
| 1 | Three lab asks listed | Pass |
| 2 | Fixtures correct | Pass |
| 3 | Explicit pre-lab-only statement | Pass |
