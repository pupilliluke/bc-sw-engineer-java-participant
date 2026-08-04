Module 19: Lab 19 page object sketch (exercise 3)


CLASS NAME

CustomerStatusPage, WebDriver and WebDriverWait fields, locators are the
exercise 2 testids and live only here.


ACTIONS

open(customerId), fillName(name), clickActivate(), submit(). clickActivate
waits for clickability before the click.


QUERIES

readStatus(), returns the badge text.


ASSERTS LIVE IN

the tests. the page returns status text and the test decides ACTIVE is
correct, a small guard assert that the page is ready is the only assert a page
method carries.


SCOPE

pre-lab only.


SELF-CHECK

fixtures confirmed, Amina CUS-1001 ACTIVE, Ravi CUS-1002 PROSPECT, correlation
lab-request-001.

| # | Confirm | Result |
| --- | --- | --- |
| 1 | File exists, lab19-page-object.md | Pass |
| 2 | Class named | Pass, under CLASS NAME |
| 3 | Actions listed | Pass, under ACTIONS |
| 4 | Assert placement noted | Pass, under ASSERTS LIVE IN |
