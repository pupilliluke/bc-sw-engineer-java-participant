# Lab 23 — Boot Starters Inventory

| Starter | Why for CRM lab |
| --- | --- |
| spring-boot-starter-web |  REST /api/customers + embedded Tomcat |
| spring-boot-starter-actuator |  /actuator/health smoke|
| spring-boot-starter-test | ContextLoads + API IT |
| spring-boot-starter-validation (optional) | @Valid bodies |



## Debug / design challenge

Does starter-actuator replace the need for starter-web?

no

## Predict the Output / Behavior

What breaks in `mvn test` if starter-test is missing?

the test starter is required for the integration tests to run, so the tests will fail if it is missing.


## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/starters.md`
- [ x ] web/actuator/test noted
- [ x ] Optional validation noted
- [ x ] No out-of-scope starters as required
