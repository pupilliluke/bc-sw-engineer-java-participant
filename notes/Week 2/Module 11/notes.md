Module 11: GitHub Copilot for Testing and Refactoring (exercise notes)

| # | Exercise | Where it lives | State |
| --- | --- | --- | --- |
| 1 | AAA template for status | below | Done |
| 2 | Notifier extract plan | below | Done |
| 3 | Trivial vs real asserts | lab11-prelab-asserts.md | Done |
| 4 | Fill acceptance checklist TODOs | lab11-acceptance-todos.md | Done |
| 5 | Coverage gaps map | lab11-coverage-gaps.md | Done |
| 6 | Lab 11 prep checklist | below | Done |

no code artifact anywhere in this module, all six are paper work.
examples\module-11-exercises was created for exercise 4 and stays empty, the
acceptance sheet turned out to be markdown too. the java lands in lab 11.


================================================================

Exercise 1: AAA Template for Status

reference table, copied from the exercise

| Phase | Northstar example |
| --- | --- |
| Arrange | Build CUS-1002 Ravi as PROSPECT |
| Act | Call activate (conceptual) |
| Assert | Status becomes ACTIVE; correlation lab-request-001 logged later |

my version, act left blank to fill in the lab

| Phase | Northstar example |
| --- | --- |
| Arrange | new CustomerService, add CUS-1002 Ravi Singh as PROSPECT |
| Act | _____ (fill in lab 11) |
| Assert | getStatus() returns ACTIVE, correlation lab-request-001 asserted later |

arrange is one customer. the test is about ravi's transition, a second fixture
just adds extra information. act is one call on purpose, if it takes two the test is covering
two things.

act is blank but the shape is known. there is no activate() on CustomerService,
checked the lab 11 starter service, the transition goes through
updateStatus(customerId, CustomerStatus.ACTIVE). activate is the story, updateStatus
is the method, put the real call in during the lab.

test name

    activate_prospectRavi_setsStatusActive

method_scenario_expected, reads as a sentence. it says activate while the method
is updateStatus, that's fine, the name describes the behaviour and the api under
it can move.

boundary

full mockito isolation is lab 18, no mock notifier and no verify() here, only the
test story. the correlation assert is out of reach for the same reason,
lab-request-001 lands in a log line and nothing is capturing logs yet.

prep note

prepare for lab 11, do not complete the full ai-assisted suite now.

pass criteria

| # | Confirm | Notes |
| --- | --- | --- |
| 1 | AAA table present | Pass |
| 2 | Test name proposed | Pass |
| 3 | Lab 18 boundary noted | Pass |


================================================================

Exercise 2: Notifier Extract Plan

smell

checked the lab 11 starter service, addCustomer ends with

    System.out.println("created " + customer.getCustomerId());

so one method validates, mutates the list and does i/o. a test can assert the
customer came back, it cannot assert the notification fired without capturing
stdout, so the side effect is untestable where it sits.

the other half of the smell, addCustomer decides how to notify. swapping print
for email means editing the service, and every test that only cared about
duplicate-id validation now runs against a method that sends mail.

extract sketch

    public interface CustomerNotifier {
        void notifyActivated(String customerId);
    }

    CustomerService takes one in the constructor
    the println becomes ConsoleCustomerNotifier
    tests hand in a fake or a mock instead

signature check. the exercise names notifyActivated(customerId), the lab 11
starter already ships CustomerNotifier with notifyCreated(String customerId,
String correlationId). two differences, the event name and the correlation
parameter. the lab's version wins when i get there. correlation as a parameter is
the right call, it keeps lab-request-001 off the Customer record and still gets it
into the log line, same split as module 10 ex 2.

why for copilot

naming the collaborator in the prompt gives copilot somewhere to put the i/o,
leave it unnamed and the suggestion buries a println in the service and the
generated test has nothing to assert against.

defer

no spring events, no kafka, no @Component. prep sketch only, the extract itself
happens in lab 11 and the mock that verifies the call waits for lab 18.

pass criteria

| # | Confirm | Notes |
| --- | --- | --- |
| 1 | Smell named | Pass |
| 2 | Interface sketched | Pass |
| 3 | Out-of-scope hosting noted | Pass |


================================================================

Exercise 6: Lab 11 Prep Checklist

fixtures from memory

| customerId | fullName | status |
| --- | --- | --- |
| CUS-1001 | Amina Khan | ACTIVE |
| CUS-1002 | Ravi Singh | PROSPECT |

checked back against the exercise 2 sketch in Module 10\notes.md rather than
trusted from memory, both rows match, no swap. that check is the point of the
step, the status swap is the one that writes itself in unnoticed.

files present

| Exercise | File | There? |
| --- | --- | --- |
| 3 | lab11-prelab-asserts.md | yes |
| 4 | lab11-acceptance-todos.md | yes |
| 5 | lab11-coverage-gaps.md | yes |

the acceptance sheet from exercise 4 exists and is filled, all six blanks
replaced, verdict Reject with the reason recorded.

boundary

pre-lab only, prepare for lab 11, do not complete the full lab 11 now.

self-mark

Pass. aaa template is in exercise 1, the acceptance todos and the gap map both
exist as their own files, so the gate in step 4 is met and there is nothing to
revisit in 2 to 5.

one thing carried into the lab, the act line in exercise 1 is still blank on
purpose. the method is updateStatus(customerId, CustomerStatus.ACTIVE), it goes
in when the starter is open.

pass criteria

| # | Confirm | Notes |
| --- | --- | --- |
| 1 | Fixtures correct | Pass |
| 2 | Pre-lab-only statement present | Pass |
| 3 | Pass/Fail self-mark written | Pass |
