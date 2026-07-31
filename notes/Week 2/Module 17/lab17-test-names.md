Module 17: Lab 17 expressive test names (exercise 1)


STEP 1, PATTERN

methodName_state_expectedOutcome, so the Surefire line names the rule that
broke without opening the file.


STEP 2, EXAMPLES

    activateCustomer_whenAlreadyActive_thenRejectsAsConflict
    activateCustomer_whenProspect_thenBecomesActive
    activateCustomer_whenIdUnknown_thenNotFound

first is Amina CUS-1001, already ACTIVE, the transition is refused and the
stored status stays ACTIVE. second is Ravi CUS-1002, PROSPECT to ACTIVE, the
allowed transition. third is CUS-9999, no such customer, a not-found failure
rather than a conflict.


STEP 3, ANTI-NAME

test1 and testActivate rejected. test1 says nothing at all, and testActivate
names the method under test, not the state or the outcome, so all three cases
above would collide on one name and a red report would not say which of them
regressed.


SCOPE

pre-lab only, do not finish the full graded lab in this exercise.


SELF-CHECK

fixtures confirmed, Amina CUS-1001 ACTIVE, Ravi CUS-1002 PROSPECT, correlation
lab-request-001.

| # | Confirm | Result |
| --- | --- | --- |
| 1 | File exists, lab17-test-names.md | Pass |
| 2 | Three good names | Pass, under STEP 2 |
| 3 | Fixtures reflected | Pass, CUS-1001, CUS-1002 and CUS-9999 under STEP 2 |
| 4 | Anti-name rejected | Pass, under STEP 3 |
