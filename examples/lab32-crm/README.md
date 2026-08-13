Northstar CRM resilience (Lab 32)

  mvn -B test
  # expect Tests run: 5, Failures: 0, Errors: 0

WireMock runs on a dynamic port inside the suite, so no Account Profile service
and no Docker are needed.

  mvn -B spring-boot:run "-Dspring-boot.run.arguments=--server.port=8092"

  curl -s http://localhost:8092/actuator/health
  curl -s http://localhost:8092/actuator/circuitbreakers
  # expect accountProfile with failureRateThreshold 50.0% and a state
  curl -s http://localhost:8092/actuator/circuitbreakerevents

Add --lab.demo=true to the run arguments to enable AccountDemoRunner, which
drives calls at a dead base-url so the breaker opens and the endpoints above
show the transitions.

  git status --short

Copied from the lab 32 starter. The starter ships the Boot baseline,
AccountSummary, AccountClient, AccountProfileService, TemporaryAccountException,
the resilience4j config and three empty tests. There is no web or service layer,
AccountProfileService is called from the tests and from the property-guarded
AccountDemoRunner. find takes correlationId as an
argument and sends it on as X-Correlation-Id, because the TimeLimiter runs the
call on another thread and a ThreadLocal would not survive it.

The starter shipped timeoutDuration: 1.5s, which Spring cannot bind to a
Duration and which fails context startup. It is 1500ms here.

The -D argument needs the quotes on PowerShell. Without them PowerShell splits
at the dot and Maven reads .run.arguments=... as a lifecycle phase.

This module is synchronous outbound HTTP. Kafka is labs 30 and 31 and is not
involved.

CLEANUP

  mvn -q clean
  git status --short

Ctrl+C spring-boot:run. target/ and .env are ignored. Keep lab32-crm.

NOTES

Configuration reasoning, annotation composition, the fallback contract and the
production caution are in docs/resilience-notes.md. Evidence and the failure
experiments are in java-bootcamp/notes/screenshots/lab-32/. Checkpoints and
reflection answers are in notes/Week 4/Module 32/lab32-answers.md. Full GUIDE at
labs/Week 4 - Kafka, React, PostgreSQL and Resilience/module-32/lab32/.
