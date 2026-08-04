Module 19: Lab 19 flake and CI note (exercise 4)


FLAKE SOURCES

1. timing and animation races, the test reads the badge before the page
   finished reacting to the click.
2. shared mutable CRM data, two tests against the one running app touching
   the same row, a UI suite shares whatever state the app holds.


MITIGATION

isolated Amina and Ravi fixtures per test, data-testid locators, explicit
waits on a real condition, no Thread.sleep.


CI CONSTRAINT

headless Chrome on the agent, WebDriverManager keeping the driver version
aligned to the installed browser.


SCOPE

pre-lab only.


SELF-CHECK

fixtures confirmed, Amina CUS-1001 ACTIVE, Ravi CUS-1002 PROSPECT, correlation
lab-request-001.

| # | Confirm | Result |
| --- | --- | --- |
| 1 | File exists, lab19-flake-ci.md | Pass |
| 2 | Two flake sources | Pass, under FLAKE SOURCES |
| 3 | Mitigation present | Pass, under MITIGATION |
| 4 | CI constraint present | Pass, under CI CONSTRAINT |
