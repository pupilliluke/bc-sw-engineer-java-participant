Module 10: Lab 10 prelab prompts (exercise 1)


WEAK PROMPT

  Write a customer class.

no jdk version, so a suggestion can reach for anything from java 8 idioms to
preview syntax and nothing flags the mismatch. no domain fixtures, so the fields
come out invented, name/email/address, none of them northstar's. no framework
rule, so @Entity and @Service arrive attached and look like they belong.


STRONG PROMPT

  Plain Java 21 record or class for the Northstar CRM customer CUS-1001
  Amina Khan status ACTIVE. Fields: id, fullName, status. No Spring, no JPA,
  no annotations, no imports outside the JDK. Put the correlation note
  lab-request-001 in a comment only, never as a field. Return compilable
  Java only.


THREE CONSTRAINTS THE STRONG PROMPT ADDS

| # | Constraint | What the strong prompt says |
| --- | --- | --- |
| 1 | JDK | Java 21, plain, no imports outside the JDK |
| 2 | Domain fixture | CUS-1001 / Amina Khan / ACTIVE named as the fixture |
| 3 | No-framework | Spring and JPA banned by name, no annotations at all |

correlation placement is the fourth, lab-request-001 goes in a comment so it
can't drift into being a customer field.


WHAT CAME BACK

record Customer(String id, String fullName, String status) with the correlation
comment on top, saved to examples\module-10-exercises\Customer.java. ran it with
java Customer.java, printed CUS-1001, Amina Khan, ACTIVE and the generated
toString. no spring, no jpa, no invented imports, so the constraints held.

sketch used customerId in exercise 2, the prompt asked for id and that's what
the file has. checked in exercise 6, lab 10 prints customerId, so the sketch was
right and the prompt was loose. worth fixing the prompt before reusing it, a
fixture that disagrees with the target code is exactly what the strong prompt is
supposed to prevent.

pre-lab only, lab 10 itself is not started.
