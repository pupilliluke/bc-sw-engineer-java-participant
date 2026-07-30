Module 14: Lab 14 invalid cases catalog (exercise 4)

negative and positive cases to assert in lab 14. sources are the slides and my
lab 12 CustomerService, plus the status list from lab 13's customer.xsd.


CREATE INVALIDS

| # | Payload | Why it fails | Caught by |
| --- | --- | --- | --- |
| 1 | fullName "", status PROSPECT | blank name | @NotBlank |
| 2 | fullName Amina Khan, status ACTVE | typo, not a known status | enum conversion |
| 3 | fullName 300 chars | oversized name | @Size max |

case 2 is the odd one. the paper DTO puts no annotation on status, so jakarta
sees a legal string and the rejection lands later when the mapper turns it into
CustomerStatus. lab 13's xsd enumerates PROSPECT, ACTIVE, SUSPENDED and CLOSED,
ACTVE is not in it.

case 3 needs a max length that does not exist yet, lab 12 only checks blankness.


ACTIVATE INVALIDS

activate with a missing id, and activate CUS-9999 which is unknown. both tie to
API errors later.

these two fail in different layers and that is the point. a null customerId is
structural, @NotNull rejects it at the boundary and the service never runs.
CUS-9999 is well formed and passes every annotation, it only fails inside
requireExisting when the map lookup misses, which the slides put in the service
layer where the business context lives.


VALID CONTROL

create a Ravi-shaped PROSPECT, CUS-1002, non-blank name, expect zero
violations and a normal save.

without a passing case the suite only proves the validator can say no.


SCOPE

pre-lab only, do not finish the full graded lab in this exercise.


SELF-CHECK

fixtures confirmed, Amina CUS-1001 ACTIVE, Ravi CUS-1002 PROSPECT, correlation
lab-request-001.

| # | Confirm | Result |
| --- | --- | --- |
| 1 | File exists, lab14-invalid-cases.md | Pass |
| 2 | At least three invalids | Pass, three create and two activate |
| 3 | One valid control | Pass, Ravi CUS-1002 PROSPECT |
| 4 | Notes saved | Pass |
