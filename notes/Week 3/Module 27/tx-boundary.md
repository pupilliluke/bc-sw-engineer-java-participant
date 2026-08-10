# Lab 27 — Transaction Boundary Placement

## Place annotation on
TransferService.transfer(...) with @Transactional

## Avoid
@Transactional on controller

## Why (one sentence)
proxy on Spring service bean; HTTP stays thin

## Self-invocation risk
this.transfer() inside same class skips proxy

## Debug / design challenge

Does a private @Transactional method participate in Spring AOP?

no

## Predict the Output / Behavior

Should SOAP and REST both call the same transactional TransferService?

yes. The transaction boundary belongs to the business operation, not the transport.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/tx-boundary.md`
- [ x ] Service placement
- [ x ] Controller avoided
- [ x ] Self-invocation noted
