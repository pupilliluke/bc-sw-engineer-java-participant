# Lab 19 — regression notes

## Environment

Chrome 150.0.7871.187 on Windows, chromedriver 150.0.7871.124 resolved by
WebDriverManager 5.9.2 at run time. Selenium 4.19.1 and the test stack come
from the Boot 3.3.5 parent. No driver binary is committed; the resolved
driver lives in the WebDriverManager cache under the user profile.

## Scope

| Suite | Boundary | Runner |
| --- | --- | --- |
| lab18-crm unit suites | service rules against mocks and the in-memory fake | Surefire, `mvn test` |
| `CustomerApiIT` | real HTTP against a running server on a random port | Failsafe, `mvn verify` |
| `CustomerUiIT` | headless Chrome through `customers.html` into the same server | Failsafe, `mvn verify` |

When the locator broke in experiment 5, `CustomerApiIT` stayed green, which
isolated the failure to the locator. The UI tests cover the fetch wiring,
the testid hooks and the rendered result region.

This module has no unit tests of its own; `mvn test` runs nothing here. The
service rules stay covered by lab18-crm's 98 tests, which this project's
thin `model.Customer` does not carry forward.

## Evidence

- API IT: `getAminaReturns200` (CUS-1001 get), `createEchoesCorrelationHeader`
  (201 + `X-Correlation-Id: lab-request-001` echoed on CUS-1005)
- UI IT: `createCustomerViaUi` (CUS-2001 through the form, result region
  asserted)
- Transcripts: `notes/screenshots/lab-19/01-mvn-clean-verify.txt` and
  `02-api-and-ui-manual.txt`

## Negative cases

- Blank name: `blankNameShowsValidationMessage` (UI) and
  `blankFullNameReturns400` (API), both permanent
- Not found: `missingCustomerReturns404` for CUS-9999
- Deliberate bad locator: red run plus capture at
  `notes/screenshots/lab-19/ui-failure.png`, restored; write-up in
  `03-failure-experiments.txt`

## CI browser strategy

Headless Chrome (`--headless=new`) with a fixed 1280x900 window, driver
resolved by WebDriverManager on the agent so the driver always matches the
installed browser. Implicit wait pinned to zero; every wait is an explicit
condition on a data-testid element or the non-blank result region. No
`Thread.sleep` anywhere in the suite; experiment 4 delayed the server by two
seconds and the explicit wait absorbed it unchanged.
