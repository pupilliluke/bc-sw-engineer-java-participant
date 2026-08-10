# Lab 29 — ErrorResponse Envelope

## Fields
timestamp, status, error, message, path, correlationId, violations

400 validation failure:

    {
      "timestamp": "2026-07-27T09:15:00Z",
      "status": 400,
      "error": "Bad Request",
      "message": "Validation failed",
      "path": "/api/customers",
      "correlationId": "lab-request-001",
      "violations": [ { "field": "email", "message": "must be a valid email" } ]
    }

404 for GET /api/customers/CUS-9999:

    {
      "timestamp": "2026-07-27T09:16:00Z",
      "status": 404,
      "error": "Not Found",
      "message": "Customer not found: CUS-9999",
      "path": "/api/customers/CUS-9999",
      "correlationId": "lab-request-001",
      "violations": []
    }

## Violation item shape
{ "field": "email", "message": "must be a valid email" }. Field name and message
only, no rejected value.

## Correlation rule
Echo X-Correlation-Id back as correlationId on every error, lab-request-001 in
the lab. Generate one when the client sends none so every response is traceable.

## Scope
Pre-lab only. Neither message contains a stack trace, a SQL fragment or an
internal class or table name.


## Debug / design challenge

Should rejected passwords appear in violations.rejectedValue?

no. the field name and the message are enough, echoing the value puts the
credential in the response body and in every log that captures it.

## Predict the Output / Behavior

Is returning only a string body enough for Lab 29 Pass?

no. the tests assert status, correlationId and violations as JSON fields.


## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/error-envelope.md`
- [ x ] Core fields
- [ x ] Violations
- [ x ] Correlation
