# Lab 27 — Transfer Pseudocode

## Annotation / method
@Transactional on TransferService.transfer(from, to, amount, correlation).
default propagation REQUIRED.

## Force-fail check
if to is ACC-FORCE-FAIL, throw before any money moves. an unchecked exception,
so it propagates out and Spring rolls back.

## Money steps
load both accounts, check funds on from, debit from, credit to. one method, so
the debit and the credit are the same unit of work.

## Log step
write TransactionLog with the correlation id last, inside the same method and
the same transaction, so it rolls back with the balances.

## Scope
Pre-lab only.


## Debug / design challenge

Where should insufficient-funds validation throw relative to debit?

before it. throwing after the debit still rolls back, but the check reads as a
rule about whether the transfer is allowed, so it belongs ahead of the first
write.

## Predict the Output / Behavior

Can the controller write TransactionLog directly?

no. it would be outside the service transaction, so the row would survive a
rollback and the controller would own a rule.


## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab27-transfer-pseudocode.md`
- [ x ] TX annotation
- [ x ] Force-fail
- [ x ] Debit/credit/log
