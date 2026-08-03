AI review log (Lab 18)

lab18-001, 2026-08-02, manual.

The duplicate-email path the guide's prompt asks for was written by hand:
aDuplicateEmailIsA409AndNeverSaves in CustomerServiceMockitoTest, existsById
stubbed false, existsByEmail stubbed true, 409 asserted with the correlation
id, existsByEmail verified, save verified never.

| # | Check | Result |
| --- | --- | --- |
| 1 | Is the class under test mocked anywhere? | Pass, no. Only CustomerRepository is a mock |
| 2 | Are the stubs minimal? | Pass, strict stubs enforce it, see experiment 4 |
| 3 | Does the verification match the real validator's call order? | Pass, see experiment 8 |
| 4 | Any Thread.sleep or real store? | Pass, none |
| 5 | mvn -B test after edits | Pass, 98 tests, run twice and in reverse order |

RISK CALLED OUT

Mocking a collaborator that holds the rule under test. CustomerValidator owns
the ALLOWED transition table, so a stubbed validateTransition does nothing and
an illegal transition is written through. Experiment 6 in
notes/screenshots/lab-18/03-failure-experiments.txt runs it: four rejections
disappear and the happy-path tests stay green. Check 1 is the same risk one
step further in, and both are the ones to run first on any generated test.
