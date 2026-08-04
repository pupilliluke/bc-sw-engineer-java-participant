Lab 19 integration and UI testing with Selenium (reflection questions,
checkpoints)

built under examples\lab19-crm from the module 19 starter, not copied from
lab18-crm. the starter's thin model.Customer has no status enum and no
validator, lab18-crm keeps the service rules. filled in the controller, the
fetch wiring, CustomerApiIT, CustomerFormPage and CustomerUiIT, added
failsafe so verify runs the IT classes. 6 tests green on 3 consecutive
mvn -B clean verify runs, Chrome 150 headless.


REFLECTION QUESTIONS

1. Which design decision most affected correctness?

the page object owning every locator. experiment 5 changed one By and only
the UI test failed, printing the missing selector, and CustomerApiIT stayed
green. inline locators would have put that string in both tests.

2. What evidence proves the implementation works?

6 IT tests green on 3 consecutive clean verifies, the correlation echo
asserted on create, and negatives that assert 400, 404 and that Ravi is
unchanged after a rejected create. the broken-locator run proves the suite
can fail, ui-failure.png captured before the failure.

3. Which failure was hardest to diagnose?

the broken locator. the TimeoutException's first line only names a lambda,
the selector is down in the caused-by NoSuchElementException, so the first
line reads as a timeout. the wrong-port failure printed
ERR_CONNECTION_REFUSED on the first line.


CHECKPOINTS

| Checkpoint | Confirm | Result |
| --- | --- | --- |
| A1 | lab19-crm under examples/ | Pass |
| A2 | Web, Selenium and WebDriverManager on the classpath | Pass, Boot 3.3.5 parent, Selenium 4.19.1, WDM 5.9.2 |
| A3 | create/get API with correlation echo | Pass, echo asserted in CustomerApiIT |
| B1 | CustomerApiIT create/get CUS-1001 | Pass, get is CUS-1001, create uses CUS-1005 so the seed is not overwritten |
| B2 | not-found 404 case | Pass, CUS-9999 |
| B3 | deterministic fixtures, no random PII | Pass, seeded Amina and Ravi, example.com addresses |
| C1 | customers.html with data-testid hooks | Pass, six hooks |
| C2 | WebDriverManager headless session, quit teardown | Pass, fresh Chrome per test |
| C3 | page object plus happy-path create | Pass, CUS-2001 per the starter, Amina is already seeded |
| C4 | blank-name negative UI assert | Pass, error text names fullName |
| D1 | two green runs, verify after trivial edit | Pass, three runs plus the title-edit run |
| D2 | failure screenshot experiment restored | Pass, ui-failure.png kept, locator restored |
| D3 | no secrets, drivers or target/ committed | Pass, .gitignore covers target/, driver stays in the WDM cache |
