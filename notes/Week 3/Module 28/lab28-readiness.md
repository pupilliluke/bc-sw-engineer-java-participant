# Lab 28 readiness checklist

| File | Present? (yes/no) |
| ---- | ----------------- |
| notes/authn-authz.md | yes |
| notes/filter-chain.md | yes |
| notes/lab28-jwt-login-todos.md | yes |
| notes/mockmvc-matrix.md | yes |
| notes/security-notes-outline.md | yes |

## Scope
Pre-lab only. Real JWT secrets in Git? no. the customer REST API from labs
25-27 runs, so security layers on top of it rather than replacing it. lab path
examples/lab28-crm, evidence under notes/screenshots/lab-28/ for the login call
and the Bearer GET on CUS-1001. global ErrorResponse unification is lab 29 and
is not built here.

## Self mark
Overall prep: Pass
If Fail, revisit: n/a


## Debug / design challenge

If authn-authz still swaps 401/403, which exercise do you reopen?

exercise 1, authn-authz.md

## Predict the Output / Behavior

Is building a React login UI required for Lab 28 timed-path Pass?

no


## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab28-readiness.md`
- [ x ] Artifacts confirmed
- [ x ] No secrets in Git
- [ x ] Pass/Fail marked