
# Lab 21 — Alert from create_failure_total

## Signal
failure rate exceeds threshold for N minutes.

## Triage steps
/actuator/health then logs by lab-request-001.

## CRM check
create path for recent traffic; confirm not a bad deploy of validation.

## Owner
on-call backend / platform.

If health is UP but failures rise, what do you check next?
Metrics 

Name one reason paging on a single failure event is a bad default.
It can lead to alert fatigue and mask the true nature of the underlying issue.


Self-check before marking Pass:

- [x] File exists at `notes/lab21-alert-runbook.md`
- [x] Signal defined
- [x] Triage present
- [x] Owner present
