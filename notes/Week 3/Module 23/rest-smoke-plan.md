# Lab 23 — REST Smoke Plan

## Start command
mvn spring-boot:run

## Health check
GET /actuator/health → UP

## CUS-1001 steps
3. POST /api/customers for CUS-1001 (Amina, ACTIVE) with correlation lab-request-001

## CUS-1002 steps
4. GET /api/customers/CUS-1002

## Correlation header/id
lab-request-002

## Debug / design challenge

If health is DOWN, should you still grade the REST steps as Pass?

no

## Predict the Output / Behavior

Where do screenshots go for evidence?

notes/screenshots/lab-23/

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/rest-smoke-plan.md`
- [ x ] Health step
- [ x ] Both customers
- [ x ] Correlation noted
