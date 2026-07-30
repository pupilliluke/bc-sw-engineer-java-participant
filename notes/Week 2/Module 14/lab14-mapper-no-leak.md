Module 14: Lab 14 mapper no-leak rule (exercise 2)

toDto and toEntity rules that keep internals out of API responses. sources are
the slides' mapping section and my lab 12 Customer and CustomerService.


STEP 1, toDto

map only customerId, fullName and status for CUS-1001 responses, nothing else.

email, phone and createdAt stay behind, so this is narrower than lab 13's
CustomerType which returns all six. toEntity is the other direction with its own
rules, it reads fullName and an optional status, createCustomer still assigns
the id and the timestamp.


STEP 2, FORBIDDEN

password hashes, internal risk scores, raw SQL ids if they differ from the
public id.

the last one does not bite yet, customerId is my only identity and equals and
hashCode are built on it. writing the rule now means a generated key added later
does not reach the wire by default.


STEP 3, ACTIVATE DTO

the activate request carries customerId only, correlation lab-request-001
travels as a header outside the body.

lab 13's xsd had correlationId as an optional child of the request element, this
moves it out. it is transport metadata, and CustomerService already appends it
to every failure message.


STEP 4, PREP BOUNDARY

DTOs before deep service rules, lab 15 owns transitions.


SCOPE

pre-lab only, do not finish the full graded lab in this exercise.


SELF-CHECK

fixtures confirmed, Amina CUS-1001 ACTIVE, Ravi CUS-1002 PROSPECT, correlation
lab-request-001.

| # | Confirm | Result |
| --- | --- | --- |
| 1 | File exists, lab14-mapper-no-leak.md | Pass |
| 2 | toDto fields listed | Pass, customerId, fullName, status |
| 3 | Forbidden fields listed | Pass, three under STEP 2 |
| 4 | Lab 15 deferral noted | Pass, under STEP 4 |
