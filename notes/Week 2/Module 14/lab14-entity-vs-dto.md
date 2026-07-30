Module 14: Lab 14 entity vs DTO (exercise 1)

why northstar's http/soap payloads should not be persistence entities. sources
are the slides and my lab 12 Customer, plus the lab 13 customer.xsd.


STEP 1, DEFINITIONS

entity = persistence shape, DTO = API contract shape.

the slides split them on scope and contents. the entity is internal and holds
persisted state including fields that should not be exposed, the DTO is
external and holds only the fields a consumer needs.


STEP 2, LEAK RISKS

internal flags and audit columns in responses.

createdAt is the audit one and it is already loose, createCustomer sets it
server-side and lab 13's CustomerType returns it anyway. internal flags are
worse, adding one to the entity publishes it, and that never looks like a
contract change while I am editing the class.


STEP 3, FIXTURE DTO FIELDS

DTO fields for Amina, customerId CUS-1001, fullName Amina Khan, status ACTIVE.
no persistence annotations.

status goes out as the enum name in a string, so CustomerStatus itself does not
cross the boundary.


SCOPE

pre-lab only, do not finish the full graded lab in this exercise.


SELF-CHECK

fixtures confirmed, Amina CUS-1001 ACTIVE, Ravi CUS-1002 PROSPECT, correlation
lab-request-001.

| # | Confirm | Result |
| --- | --- | --- |
| 1 | File exists, lab14-entity-vs-dto.md | Pass |
| 2 | Definitions written | Pass, under STEP 1 |
| 3 | Two leak risks | Pass, internal flags and audit columns |
| 4 | Amina DTO fields listed | Pass, customerId, fullName, status |
