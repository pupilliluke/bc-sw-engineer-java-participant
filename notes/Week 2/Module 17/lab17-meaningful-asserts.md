Module 17: Lab 17 meaningful asserts (exercise 3)


STEP 1, WEAK

    assertNotNull(result);

weak. after activate it only proves a Customer came back. it still passes if
the status never moved, if the wrong customer was returned, or if the id and
the email were overwritten.


STEP 2, STRONG

Ravi CUS-1002, PROSPECT to ACTIVE:

    assertEquals("CUS-1002", result.getCustomerId());
    assertEquals(CustomerStatus.ACTIVE, result.getStatus());

the id assert says the right record came back, the status assert says the rule
fired. reading the stored customer back from the repository and asserting
ACTIVE there as well proves the change was saved, not just returned.


STEP 3, EXCEPTION ASSERT

Amina CUS-1001 is already ACTIVE and my policy rejects a transition to the
status a customer already holds:

    BusinessException ex = assertThrows(
            BusinessException.class,
            () -> service.changeStatus("CUS-1001", CustomerStatus.ACTIVE,
                    "lab-request-001"));
    assertEquals("BUSINESS_CONFLICT", ex.getCode());
    assertEquals(409, ex.getStatusHint());
    assertEquals("lab-request-001", ex.getCorrelationId());

then assert the stored status is still ACTIVE, so the rejected call left no
half-applied change behind. asserting the code rather than the message keeps
the test from breaking when the wording changes.


STEP 4, PREP ONLY

prepare for lab 17, do not complete the full suite now.


SCOPE

pre-lab only, do not finish the full graded lab in this exercise.


SELF-CHECK

fixtures confirmed, Amina CUS-1001 ACTIVE, Ravi CUS-1002 PROSPECT, correlation
lab-request-001.

| # | Confirm | Result |
| --- | --- | --- |
| 1 | File exists, lab17-meaningful-asserts.md | Pass |
| 2 | Weak vs strong shown | Pass, STEP 1 against STEP 2 |
| 3 | assertThrows planned | Pass, under STEP 3 |
| 4 | Pre-lab boundary present | Pass, under STEP 4 |
