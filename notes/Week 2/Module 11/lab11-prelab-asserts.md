Module 11: Lab 11 prelab asserts (exercise 3)

fixtures are the module 10 ones, CUS-1001 Amina Khan ACTIVE and CUS-1002 Ravi
Singh PROSPECT. code references are the lab 11 starter under
labs\Week 2 - Backend, AI Tools and Testing\module-11\lab11\starter.


TRIVIAL

    assertNotNull(customer)
    assertTrue(true)

assertTrue(true) cannot fail, it asserts nothing about the code. assertNotNull
looks better but only proves addCustomer returned a reference, and it returns the
same object it was handed, so the assert passes even if the method never stored
the customer and never set a status. both go green against a service that does
nothing.


MEANINGFUL

    assertEquals(CustomerStatus.ACTIVE,
        service.findByCustomerId("CUS-1001").orElseThrow().getStatus())

    assertEquals(CustomerStatus.PROSPECT,
        service.findByCustomerId("CUS-1002").orElseThrow().getStatus())

these read through the service instead of the returned reference, so they prove
the customer was actually stored, and they name the fixture and the outcome, so a
swapped status fails.


THE TRAP IN BETWEEN

checked Customer in the lab 11 starter, equals compares customerId only

    if (!(o instanceof Customer other)) return false;
    return Objects.equals(customerId, other.customerId);

so assertEquals(expected, actual) on two Customer objects passes when the ids
match and the statuses differ. that assert names Amina, uses CUS-1001 and still
proves nothing about status. naming the fixture is not enough on its own, assert
on getStatus(), not on the record.


REVIEW RULE

reject any ai test whose asserts never name a domain value or an outcome.


BOUNDARY

pre-lab only, prepare for lab 11, do not complete the full ai-assisted suite now.


PASS CRITERIA

| # | Confirm | Notes |
| --- | --- | --- |
| 1 | Two trivial and two meaningful asserts | Pass |
| 2 | Fixtures used correctly | Pass |
| 3 | Notes saved | Pass |
