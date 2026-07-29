Module 11: Lab 11 acceptance todos (exercise 4)

the checklist is filled against one sample ai test for the first starter todo,
addAndFindAminaKhan in
labs\Week 2 - Backend, AI Tools and Testing\module-11\lab11\starter\src\test\java\com\northstar\crm\CustomerServiceTest.java

the sample below is written by hand in the shape copilot usually returns for that
todo, the real generation happens in the lab. the checklist is the point of the
exercise, the sample just gives it something to score.


SAMPLE UNDER REVIEW

    @Test
    void testAddCustomer() {
        CustomerService service = new CustomerService();
        Customer customer = new Customer();
        customer.setCustomerId("CUS-1001");
        customer.setFullName("Amina Khan");

        Customer result = service.addCustomer(customer);

        assertNotNull(result);
    }


ACCEPTANCE TODOS

AAA structure present? yes — arrange, act and assert are in order and the act is
a single call. no comment banners, but the shape holds without them.

Asserts use Amina/Ravi values? no — CUS-1001 and Amina Khan are set up in arrange
and never appear in an assert. the fixture is decoration here.

Trivial assertNotNull-only rejected? yes, rejected — assertNotNull(result) is the
only assert and addCustomer returns the same object it was handed, so it passes
even if the customer was never stored. it also never touches status, which
addCustomer defaults to PROSPECT.

Correlation mention lab-request-001? no — nothing logs it and nothing asserts it.
addCustomer prints "created " + id and no correlation id reaches the line.

Coverage gap noted for Labs 17-18? yes — see the gap sentence below.

Accept / Reject: Reject


WHAT WOULD MAKE IT PASS

read the customer back through the service instead of asserting on the returned
reference, and assert a domain value

    assertEquals(CustomerStatus.ACTIVE,
        service.findByCustomerId("CUS-1001").orElseThrow().getStatus());

name it method_scenario_expected, testAddCustomer says nothing. same rule as
exercise 1.


GAP

Deep Mockito verify waits for Lab 18; JaCoCo gates deepen in Lab 17.

so the correlation line stays unasserted for now, nothing is capturing logs and
there is no mock notifier yet. the reject above is about the asserts, not about
the missing verify, that one is out of scope on purpose.


SELF-CHECK

| Check | Value | Confirm | Result |
| --- | --- | --- | --- |
| Blanks | all six replaced | every _____ filled | Pass |
| Gap | labs 17 and 18 both named | one sentence, both labs | Pass |
| Verdict | Reject | recorded, with the reason | Pass |

pre-lab only, prepare for lab 11, do not complete the full lab now.
