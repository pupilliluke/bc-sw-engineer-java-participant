Module 10: Lab 10 review-log todos (exercise 4)

reviewed suggestion: examples\module-10-exercises\Customer.java, generated from
the strong prompt in lab10-prelab-prompts.md.


REVIEW LOG

Prompt strength: strong — jdk 21 named, CUS-1001 Amina Khan ACTIVE given as the
fixture, spring and jpa banned by name, correlation note pinned to a comment.

Phantom annotation found? no — the suggestion carried no annotations and no
imports at all. @NorthstarEntity is my own planted trap row in exercise 3, it
never appeared in generated code.

Fixture check Amina status: ACTIVE — matches the sketch, hard-coded in the
main that builds CUS-1001.

Fixture check Ravi status: PROSPECT — not present in this file, so nothing to
correct yet. lab 10 adds CUS-1002 and this line gets checked for real.

JDK/Maven note: JDK 21.0.4 Temurin, maven 3.9.9 on the same jdk. ran as a
single file with java Customer.java, no pom and no dependency added, nothing
imported from outside the jdk.

Accept / Reject / Edit: Edit


EDIT REASON

no invented api and no wrong fixture, so the reject triggers didn't fire. edited
because the bare record shipped with no validation, a blank id or status would
have constructed fine, and the field name came back as id while the exercise 2
sketch says customerId. added a compact constructor rejecting null and blank on
all three fields, plus a main that builds CUS-1001 so the fixture is actually
exercised.


SELF-CHECK

| Check | Value | Confirm | Result |
| --- | --- | --- | --- |
| Amina | ACTIVE | matches sketch, not swapped | Pass |
| Ravi | PROSPECT | matches sketch, not swapped | Pass |
| Blanks | all six replaced | every _____ filled | Pass |

read the two status lines back against exercise 2 rather than from memory, that
swap is the easy one to write in without noticing.
