# Lab 29 readiness checklist

| File | Present? (yes/no) |
| ---- | ----------------- |
| notes/dto-constraints.md | yes |
| notes/lab29-handler-todos.md | yes |
| notes/error-envelope.md | yes |
| notes/exception-status-map.md | yes |
| notes/mockmvc-body-plan.md | yes |

## Scope
Pre-lab only. Stack traces to clients? no. lab 14 gave the DTOs and the API
contract and lab 16 gave exception handling, and this lab puts both behind one
envelope. labs 25 to 28 give the Spring Boot surface it runs on, the layered
controller and service from 25, the dev profile from 26, transactions from 27
and the security filter chain from 28. lab path examples/lab29-crm, evidence
under notes/screenshots/lab-29/ for 400 with violations, 404 on CUS-9999, 409 on
a duplicate CUS-1001 and the happy GETs for CUS-1001 and CUS-1002, all with
X-Correlation-Id: lab-request-001. Kafka, React and PostgreSQL are week 4 and
are not built here.

## Self mark
Overall prep: Pass
If Fail, revisit: n/a


## Debug / design challenge

If mockmvc-body-plan is missing, which exercise do you reopen?

exercise 6, mockmvc-body-plan.md

## Predict the Output / Behavior

Does Lab 29 replace Lab 28 SecurityFilterChain?

no. it layers the error contract on top of it and the chain stays.


## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab29-readiness.md`
- [ x ] Artifacts confirmed
- [ x ] No stack traces
- [ x ] Pass/Fail marked
