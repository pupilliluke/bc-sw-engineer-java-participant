# Lab 27 — ACID for CRM Transfers

| Letter | CRM observation |
| --- | --- |
| A |  Forced fail leaves MAIN unchanged; no success log |
| C | After happy path, balances and log agree |
| I | Default isolation; no dirty mid-transfer reads required for Pass |
| D | Committed happy path survives restart (note H2 mode) |


## Debug / design challenge

If a success log row exists after ACC-FORCE-FAIL, which ACID letter failed?

C

## Predict the Output / Behavior

Is “we used @Transactional” enough evidence for Atomicity?

No

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/acid-crm.md`
- [ x ] All four letters
- [ x ] CRM observations
