Northstar CRM build (Lab 11)

  mvn -q clean package
  java -jar target\customer-service.jar

Prints all customers, the PROSPECT filter, then CUS-1002 after activation, the
same harness Lab 10 ended on. Artifact is
com.northstar:customer-service:0.1.0-SNAPSHOT, packaged as
target/customer-service.jar. Same coordinates as Labs 9 and 10, the project
carries forward rather than forking.

TESTS

  mvn -q clean test

Eight tests, CustomerTest 2, CustomerServiceTest 5, CustomerNotifierMockTest 1.
JUnit 5 and Mockito are test scope only, so they stay off the runtime classpath
and out of the jar.

WHAT LAB 11 ADDED

CustomerNotifier, a one-method interface CustomerService calls after a status
change, so the transition can be verified with a mock instead of watched as a
println. validateCustomerId, the single blank-id check that addCustomer,
updateStatus and deleteCustomer all route through. Everything else is Lab 10
carried across unchanged, including the controller, repository and DTO stubs
that the Lab 11 starter had dropped.

CI

  mvn -B verify

Batch mode, non-interactive, stops after verification. Prefer it over install
on shared agents, install writes into that agent's ~/.m2 where every other job
can see it.

CLEANUP

  mvn clean

NOTES

copilot-notes/ai-test-refactor-notes.md is this lab, lab11-001 to lab11-004
plus the failure experiments. copilot-notes/ai-review-notes.md is Lab 10's
review log, carried across so the audit trail for CustomerService reads in one
place. Checkpoints and reflections in notes/Week 2/Module 11/lab11-answers.md.
Full GUIDE at labs/Week 2 - Backend, AI Tools and Testing/module-11/lab11/.


## 45-minute checklist

- [x] Complete `CustomerTest` + `CustomerServiceTest` TODOs (reject weak assertions)
- [x] Flesh out `CustomerNotifier`; wire into service; Mockito verify
- [x] Extract duplicated validation helper
- [x] Fill `copilot-notes/ai-test-refactor-notes.md` lab11-001–004
- [x] Run smoke test

## Smoke test

```bash
mvn -B test
```

## Timed-path Pass criteria

| Criterion | Pass / Fail |
| --------- | ----------- |
| `mvn test` green (≥ a few meaningful assertions) | Pass, 8 tests, every assertion names a domain value |
| Mockito verifies notifier (or documented equivalent) | Pass, `notifyStatusChange` PROSPECT to ACTIVE, plus `verifyNoMoreInteractions` |
| Notes show at least one rejected trivial test | Pass, two, both in `lab11-001` |

Full path done as well, not just the timed core. Steps 1 to 9, checkpoints A to
D in `notes/Week 2/Module 11/lab11-answers.md`, failure experiments and the
security review in `copilot-notes/ai-test-refactor-notes.md`.
