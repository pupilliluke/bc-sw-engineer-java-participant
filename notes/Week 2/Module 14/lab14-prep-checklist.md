Module 14: Lab 14 prep checklist (exercise 6)

confirming the earlier notes exist and lab 14 is ready to open.


EARLIER EXERCISE FILES PRESENT?

| File | Present? (yes/no) |
| ---- | ----------------- |
| notes/lab14-entity-vs-dto.md | yes |
| notes/lab14-mapper-no-leak.md | yes |
| notes/lab14-annotate-dto.md | yes |
| notes/lab14-invalid-cases.md | yes |
| notes/lab14-validatorfactory-todos.md | yes |

all five sit under notes\Week 2\Module 14, same layout as module 13.


FIXTURES (VERIFY)

| ID | Name | Status |
| -- | ---- | ------ |
| CUS-1001 | Amina Khan | ACTIVE |
| CUS-1002 | Ravi Singh | PROSPECT |

correlation lab-request-001, which stays in headers and logs rather than in a
DTO field.


SCOPE STATEMENT

pre-lab only, prepare for lab, do not complete full lab 14 now. DTOs before deep
service rules, lab 15 owns transitions. no spring @Valid live in this prep, the
validator is built by hand.


SELF MARK

overall prep, Pass.

going in I have the entity/DTO split, the three-field toDto rule, the paper
annotations, five cases to assert and the bootstrap two-liner. the two things
lab 14 still has to settle are a max length for fullName and moving customerId
to server-assigned, since lab 12 takes it as a create parameter today.
