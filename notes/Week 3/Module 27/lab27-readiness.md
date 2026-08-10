# Lab 27 readiness checklist

| File | Present? (yes/no) |
| ---- | ----------------- |
| notes/acid-crm.md | yes |
| notes/tx-boundary.md | yes |
| notes/rollback-plan.md | yes |
| notes/lab27-transfer-pseudocode.md | yes |
| notes/propagation-warnings.md | yes |

## Scope
Pre-lab only. @Transactional on controller? no. lab 25 gave the layered
service and repository structure and lab 26 gave the dev H2 profile, so both
carry forward. lab path examples/lab27-crm, evidence under
notes/screenshots/lab-27/. JWT on the transfer routes is lab 28 and is not
built here.

## Self mark
Overall prep: Pass
If Fail, revisit: n/a


## Debug / design challenge

If tx-boundary still puts TX on the controller, which exercise do you reopen?

exercise 2, tx-boundary.md

## Predict the Output / Behavior

Is Kafka saga required for Lab 27 timed-path Pass?

no


## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab27-readiness.md`
- [ x ] Artifacts confirmed
- [ x ] No controller TX
- [ x ] Pass/Fail marked
