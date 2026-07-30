Lab 14 DTOs and validation (key ideas, reflection questions, checkpoints, manual
verification, failure experiments)

built under examples\lab14-crm, copied forward from lab12-crm. two DTOs, a
mapper, a facade, three new test classes, twenty-two tests green.


KEY IDEAS

1. Main data flow

facade takes a CustomerRequestDTO and a correlation id, validates it, maps it to
a Customer, hands that to CustomerService, maps the saved entity back to a
CustomerResponseDTO. four steps in a fixed order, and the entity exists only
between steps two and four.

2. Trust boundary

the boundary is CustomerApiFacade.create, everything before it is untrusted.
lab 12's requireNonBlank and requireUniqueId are still there as a second layer,
but they are a backstop now, not the gate.

3. Success and failure contract

success returns a response DTO. failure throws IllegalArgumentException carrying
a stable code and the correlation id. three codes, CUSTOMER_VALIDATION_FAILED
for a malformed payload, CUSTOMER_STATUS_INVALID for a status the enum does not
know, CUSTOMER_NOT_FOUND for a missing id. duplicate create is the odd one out,
it still throws IllegalStateException from the service in lab 12's format.

4. Stable identity vs mutable fields

customerId is the identity, it is what equals and hashCode use and what the
store is keyed on. fullName, email, phone and status are display state and can
change without the record becoming a different customer.

5. Retry and idempotency

get is safe to retry. create is not, a second identical call throws rather than
overwriting, which is the same decision lab 12 made. there is no idempotency key
on the request, so a client that retries after a timeout cannot tell a duplicate
from a genuine conflict. that gap is real and this lab does not close it.

6. Programmatic validator vs spring @Valid

Validation.buildDefaultValidatorFactory().getValidator(), called by hand in the
facade constructor. @Valid does not do the work itself, it marks a parameter for
a framework to call this same API, and there is no framework here to do it.


REFLECTION QUESTIONS

1. Which design decision most affected correctness?

keeping status as a String instead of typing it as CustomerStatus. it means a
typo reaches the mapper rather than dying in deserialization, so the rejection
carries a stable code and a field name. it also means zero violations does not
prove a payload is good.

2. What evidence proves the implementation works?

twenty-two green tests, and specifically the mockito one asserting
CustomerService is never called for a blank name. the Main transcript shows both
fixtures returned as response DTOs and five failures each carrying
lab-request-001. output is under notes/screenshots/lab-14.

3. Which failure was hardest to diagnose?

none of the real ones, but experiment 4 was the most surprising. removing the
validate call did not break the blank-name case, lab 12's guard caught it, so
the suite looked healthier than it was. the invalid email saved silently.


CHECKPOINTS

| Checkpoint | Confirm | Result |
| --- | --- | --- |
| A1 | lab14-crm under examples/ | Pass |
| A2 | validation api, hibernate validator, expressly resolve | Pass |
| A3 | CustomerRequestDTO annotations compile, jakarta | Pass |
| B1 | CustomerResponseDTO and CustomerMapper present | Pass |
| B2 | facade validates before service calls | Pass, proved by the mockito test |
| B3 | correlation id appears on validation failures | Pass |
| C1 | validation tests green | Pass, 5 in CustomerRequestDTOValidationTest |
| C2 | Main creates and gets both fixtures as response DTOs | Pass |
| C3 | no facade method returns Customer | Pass, both return CustomerResponseDTO |
| D1 | README constraint table and run instructions | Pass |
| D2 | failure experiments recorded | Pass, five, all run and restored |
| D3 | no secrets or target/ staged | Pass, target/ is gitignored |


MANUAL VERIFICATION

| # | Check | Result |
| --- | --- | --- |
| 1 | create and read for CUS-1001 and CUS-1002 | Pass |
| 2 | invalid email, blank name, oversized id rejected at the facade | Pass |
| 3 | API returns CustomerResponseDTO, never Customer | Pass |
| 4 | lab-request-001 on validation and not-found errors | Pass |
| 5 | validation tests pass independently of service tests | Pass |
| 6 | duplicate create still a service rule | Pass, experiment 3 |
| 7 | no secrets in logs or git | Pass |
| 8 | README lists constraints and commands | Pass |
| 9 | mvn -B clean test succeeds | Pass, 22 tests |
| 10 | can explain why entities stay behind the mapper | Pass, docs/dto-boundary-notes.md |


FAILURE EXPERIMENTS

1. removed hibernate-validator from the pom. NoProviderFoundException out of
buildDefaultValidatorFactory, so every validation test errored in setUp rather
than failing an assertion. the annotations compiled fine without it, which is the
point, the api is separate from the implementation. restored, 22 green.

2. bad email, blank name and blank status through Main. all three rejected at the
facade with the correlation id, CustomerService never entered.

3. created CUS-1001 twice. IllegalStateException from requireUniqueId, lab 12's
message, no stable code and no field path. a duplicate is a legal payload that
conflicts with the store, so it cannot be an annotation.

4. commented out the validate call. the invalid email saved with no failure at
all, nothing below the boundary looks at email. the blank name still failed, but
from the service and in the older message format. restored.

5. status ACTVE. zero violations, then valueOf threw and the facade turned it
into CUSTOMER_STATUS_INVALID. matches the prediction in
lab14-validatorfactory-todos.md.
