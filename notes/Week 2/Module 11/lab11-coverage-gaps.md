Module 11: Lab 11 coverage gaps (exercise 5)

what lab 11 prep will and will not prove, checked against the starter pom and the
starter test class rather than guessed.


IN SCOPE SOON, LAB 11

ai-assisted happy-path test sketch, the addAndFindAminaKhan todo in
CustomerServiceTest, generated with copilot and read back before it is trusted.

aaa discipline, arrange one fixture, act one call, assert the outcome. the shape
from exercise 1.

rejecting trivial asserts, assertNotNull and assertTrue(true) do not count as
coverage. the rule from exercise 3.

the refactor half, extracting CustomerNotifier and the validation helper out of
addCustomer. the sketch from exercise 2.


DEFERRED TO LAB 17

parameterized tests, @ParameterizedTest and @CsvSource over the status
transitions instead of one hard-coded case per test.

naming conventions as a taught standard. method_scenario_expected gets used in
lab 11, it gets applied across a suite in lab 17.

jacoco and the coverage narrative. checked the starter pom, there is no jacoco
plugin in it at all, only compiler, surefire and jar. so lab 11 cannot produce a
coverage number even informally, and no percentage gate exists to argue about
until the plugin lands.


DEFERRED TO LAB 18

stub vs verify, the difference between when(...).thenReturn(...) standing in for
a collaborator and verify(...) asserting the call happened.

ArgumentCaptor, which is what finally makes the lab-request-001 correlation
assertable. the id rides a log line today and nothing captures it.

worth noting, mockito-core and mockito-junit-jupiter 5.14.2 are already on the
test classpath in the starter pom, and CustomerServiceTest carries a todo saying
add a Mockito test verifying CustomerNotifier.notifyCreated when wired. the
dependency being present is not the same as the technique being taught. the
mock notifier test is lab 18's, not something to sneak into lab 11 because the
jar is there.


BOUNDARY

pre-lab only, prepare for lab 11, do not complete the full lab now.


PASS CRITERIA

| # | Confirm | Notes |
| --- | --- | --- |
| 1 | Lab 11 prep items listed | Pass |
| 2 | Lab 17 and 18 deferred items named | Pass |
| 3 | Notes file saved | Pass |
