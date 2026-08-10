# Lab 28 — MockMvc Evidence Matrix

| Case | Auth | Route | Expect |
| --- | --- | --- | --- |
| Anonymous customers |none | GET /api/customers/CUS-1001 | 401 |
| Agent admin | AGENT | GET /api/admin/... | 403 |
| Agent customer | AGENT | GET /api/customers/CUS-1001 | 200 |
| Bad token | garbage | GET /api/customers/CUS-1001 | 401 |

## Debug / design challenge

Should login success be part of the matrix or a separate test?

separate test. All first time login is new user

## Predict the Output / Behavior

Why test bad token separately from missing token?

I dont know, both 401

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/mockmvc-matrix.md`
- [ x ] 401 case
- [ x] 403 case
- [ x ] 200 case
