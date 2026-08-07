# Lab 24 — SOAP Fault Versus REST Error

| Case | SOAP | REST |
| --- | --- | --- |
| Missing customer | SOAP fault, faultcode Client, faultstring Customer not found | 404 with a JSON error body |
| Validation fail | SOAP fault, faultcode Client, short message | 400 with a JSON error body |
| Missing UsernameToken | wss4j security fault raised before the endpoint runs | 401, lab 28 work |

CUS-9999 is the not-found case on both.

## One rule
one service exception, two protocol adapters. mapping happens in the
SoapFaultMappingExceptionResolver for SOAP and the advice for REST, and neither
faultstring, detail nor the json body carries a stack trace, class name or secret.

## Scope
Pre-lab only.

## Debug / design challenge

Should CustomerEndpoint catch Exception and always return a generic SERVER fault?

no. not-found and duplicate are the caller's fault and map to Client. SERVER is
the default for what is left.

## Predict the Output / Behavior

Where should NotFoundException be translated for SOAP?

in the fault mapping resolver, not inside the endpoint method.

## Pass criteria

Self-check before marking Pass:

- [x] File exists at `notes/fault-vs-rest.md`
- [x] Table filled
- [x] Shared exception rule
