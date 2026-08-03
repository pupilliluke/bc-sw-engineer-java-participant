Module 18: Lab 18 Mockito anti-patterns (exercise 5)


REFERENCE

| Anti-pattern | Better |
| --- | --- |
| Mock the SUT | Mock collaborators only |
| Unnecessary stubbing | Stub what is used |
| verifyNoMoreInteractions always | Use when interaction surface is critical |
| Mock a String or a CustomerStatus | Use the real value, ACTIVE is one enum constant |

the added row is the silly one and the easiest to spot. CustomerStatus has four
constants and no behaviour to control, a mock of it can only return what a
plain reference already returns.


STEP 2, AI REJECT RULE

reject any suggestion that mocks DefaultCustomerService while the test is
testing DefaultCustomerService, no real logic runs and the test asserts the
stub.

three more I expect to reject in this lab:

mocking CustomerValidator. it holds the ALLOWED transition table, the rule the
activate tests exist to check, so stubbing it lets a wrong transition pass.

stubs the test never calls. MockitoExtension runs strict stubs, so an unused
when() fails the run with UnnecessaryStubbingException rather than sitting
there quietly, but the draft still has to be read and cut.

Thread.sleep anywhere in a unit test. nothing in these tests is asynchronous,
a sleep only makes the suite slower and hides a design problem.

every accepted or rejected suggestion goes in the copilot log with the prompt
and the reason, same format as lab 17.


STEP 3, FIXTURE

real fixtures for Amina CUS-1001 ACTIVE and Ravi CUS-1002 PROSPECT, built from
the Customer constructor by the same private helpers my lab 17 tests use. a
mocked Customer with a stubbed getStatus would also break equals, which is
customerId only, so verify and the captor would stop meaning what they mean.
mock the boundary, keep the domain objects real.


STEP 4, BOUNDARY

ArgumentCaptor gets its deep practice in the timed lab, the preview is exercise
3.


SCOPE

pre-lab only, do not finish the full graded lab in this exercise.


SELF-CHECK

fixtures confirmed, Amina CUS-1001 ACTIVE, Ravi CUS-1002 PROSPECT, correlation
lab-request-001.

| # | Confirm | Result |
| --- | --- | --- |
| 1 | File exists, lab18-anti-patterns.md | Pass |
| 2 | Table plus silly mock row | Pass, four rows under REFERENCE |
| 3 | SUT-mock reject rule | Pass, under STEP 2 |
| 4 | Real fixture preference noted | Pass, under STEP 3 |
