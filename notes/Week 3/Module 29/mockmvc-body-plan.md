# Lab 29 — MockMvc Body Assertions Plan

| Case | Status | Body asserts |
| --- | --- | --- |
| Bad email | 400 | status=400, error=Bad Request, correlationId exists, violations is an array and not empty, violations contains field email |
| CUS-9999 | 404 | status=404, message names CUS-9999, correlationId exists |
| Duplicate | 409 | status=409, message names CUS-1001, correlationId exists |
| GET CUS-1001 | 200 | id=CUS-1001, name=Amina Khan, status=ACTIVE, no envelope fields |

## Scope
Pre-lab only. Lab 28 security is on these routes, so each request needs a Bearer
header. The token comes from a login in the test, security is not disabled to
make an assertion pass.


## Debug / design challenge

Why sort field violations in the handler or loosen order asserts?

the validator does not guarantee the order of the field errors, so an assert on
violations[0].field flakes between runs. sort by field name in the handler, or
assert that the array contains the field without an index.

## Predict the Output / Behavior

Should 401 from Lab 28 be tested in the same class?

no. SecurityPathTest already covers 401 and 403. this class asserts the error
envelope.


## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/mockmvc-body-plan.md`
- [ x ] 400/404/409 cases
- [ x ] Body asserts
