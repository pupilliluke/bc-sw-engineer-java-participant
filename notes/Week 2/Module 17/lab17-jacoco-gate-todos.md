Module 17: Lab 17 JaCoCo gate narrative (exercise 4)


STEP 1, COPY TODOS

Tool: JaCoCo
Maven phase idea: prepare-agent before test, check bound to verify
Target line coverage % (lab goal): 80
Package to measure: com.northstar.crm.service
Gap you still expect: the service-layer validation backstop
Mockito depth in this pre-lab? no


STEP 2, FILL BLANKS

80 is the module's diagnostic target, not the definition of done. the agent has
to be attached before the tests run or the report comes back empty, and the
check goal belongs on verify so a thin suite fails the build rather than being
noticed later.

com.northstar.crm.service is the package worth gating because it holds
CustomerValidator and DefaultCustomerService, the transition matrix and the
duplicate rules. gating the entity or the dto package would inflate the number
with getters.

the gap I expect is the validation backstop in the service, the branches that
throw for a null customer or a blank customerId. requests coming through the
facade are rejected at the edge before they reach it, so those lines stay red
unless a test calls the service directly. that is an honest gap and I would
justify it rather than delete the guard.

coverage does not prove the assertions were meaningful. every branch of the
transition matrix can execute under assertNotNull alone and still report 80.


STEP 3, AAA PLAN LINE

AAA service tests planned, collaborators real or simple fakes until lab 18.


STEP 4, SELF-CHECK

Mockito depth blank is no. the fake repository from my earlier labs is enough
for lab 17, and no Selenium here either, UI automation is lab 19.


SCOPE

pre-lab only, do not finish the full graded lab in this exercise.


SELF-CHECK

fixtures confirmed, Amina CUS-1001 ACTIVE, Ravi CUS-1002 PROSPECT, correlation
lab-request-001.

| # | Confirm | Result |
| --- | --- | --- |
| 1 | File exists, lab17-jacoco-gate-todos.md | Pass |
| 2 | All blanks replaced | Pass, six of six under STEP 1 |
| 3 | AAA plan line present | Pass, under STEP 3 |
| 4 | Mockito deferred | Pass, no under STEP 1 and STEP 4 |
