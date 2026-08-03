Module 18: Lab 18 prep checklist (exercise 6)


EARLIER EXERCISE FILES PRESENT?

| File | Present? (yes/no) |
| ---- | ----------------- |
| notes/lab18-keep-real-validator.md | yes |
| notes/lab18-stub-verify.md | yes |
| notes/lab18-argumentcaptor-preview.md | yes |
| notes/lab18-activate-interaction-todos.md | yes |
| notes/lab18-anti-patterns.md | yes |

all five, under notes\Week 2\Module 18 rather than
examples\module-18-exercises\notes, same as every module since week 1. the
exercises produced notes only, no java, so the examples workspace the index
sets up was never needed.

the captor preview is the one to check twice, it is the piece lab task 5
depends on: declare, capture inside verify, assert the captured status.


FIXTURES (VERIFY)

| ID | Name | Status |
| -- | ---- | ------ |
| CUS-1001 | Amina Khan | ACTIVE |
| CUS-1002 | Ravi Singh | PROSPECT |

correlation lab-request-001. CUS-9999 stays out of the table on purpose, it is
the id that was never created, and in this lab it is what proves isolation,
findById stubbed empty and verify(repository, never()).save(any()).


SCOPE STATEMENT

pre-lab only, prepare for lab, do not complete full lab 18 now.

Selenium and the test pyramid are lab 19, nothing in these notes touches UI
automation. repository behaviour against a real store is an integration
concern and is tested elsewhere, Mockito here only proves what
DefaultCustomerService does with its port.


SELF MARK

overall prep, Pass. no exercise to revisit.

| # | Confirm | Result |
| --- | --- | --- |
| 1 | File exists, lab18-prep-checklist.md | Pass |
| 2 | Artifacts confirmed | Pass, five of five listed and checked |
| 3 | Pre-lab-only statement | Pass, under SCOPE STATEMENT |
| 4 | Lab 19 pointer present | Pass, under SCOPE STATEMENT |
