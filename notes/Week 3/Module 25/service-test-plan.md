# Lab 25 — Service Test Plan

| Case | Setup | Expect |
| --- | --- | --- |
| get CUS-1001 | seeded repository | Amina Khan, ACTIVE |
| duplicate create | CUS-1001 already in the store | IllegalStateException duplicate |
| get CUS-9999 | id not in the store | not-found |
| create new | CUS-2501 not in the store | saved, then readable by get |

## Spring Boot required for unit test?
no. the repository arrives through the constructor, so a fake repository or the
real InMemoryCustomerRepository both work under plain JUnit.

## Scope
Pre-lab only.


## Debug / design challenge

Why prefer a fresh repository per @BeforeEach?

so a create in one test cannot leave CUS-2501 behind and change what the next
test sees.

## Predict the Output / Behavior

Should these unit tests call CustomerController?

no. service level only.


## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/service-test-plan.md`
- [ x ] Four cases
- [ x ] No Boot required noted
