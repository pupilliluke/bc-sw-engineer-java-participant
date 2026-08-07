# Lab 24 — UsernameToken Plan

## Where credentials live
in the soap header, a wsse:Security block holding a UsernameToken with Username
crm-partner and Password Type PasswordText lab24-shared-secret. a lab secret, not
a real one.

## Success case
the secured request carries the token, the wss4j interceptor validates it, then
GetCustomer for CUS-1001 runs and returns Amina Khan ACTIVE under lab24-001.

## Failure case
the unsecured request has no Security header, wss4j rejects it and returns a
security fault before the endpoint method is reached. that fault reads
differently from the CUS-9999 not-found fault.

## Out of scope
signatures, encryption, timestamps, SAML and X.509 tokens, a production idp, and
bearer JWT which is lab 28 on REST.

## Scope
Pre-lab only.

## Debug / design challenge

Is PasswordText UsernameToken enough without HTTPS in production?

no. the password crosses the wire in plaintext. production wants TLS underneath
plus a digest, and rotated secrets.

## Predict the Output / Behavior

Does UsernameToken replace constructor DI on CustomerService?

no. it decides who gets in, the endpoint still needs the service injected.

## Pass criteria

Self-check before marking Pass:

- [x] File exists at `notes/usernametoken-plan.md`
- [x] Header location
- [x] Success + failure
- [x] Out of scope noted
