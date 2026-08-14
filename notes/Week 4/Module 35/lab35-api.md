# Lab 35 — Endpoint Map

## Reference

| UI action | HTTP |
| --- | --- |
| List customers | GET /api/customers |
| Open Amina | GET /api/customers/CUS-1001 |
| Create customer | POST /api/customers |
| Update status | PATCH /api/customers/{id} |

same base path the CRM controllers have used since the Spring labs,
@RequestMapping("/api/customers") with GET list, GET /{id}, POST create,
and lab 25 added PATCH /{id}/status. the browser speaks JSON to these
paths only, it never calls Kafka topics directly.

## Step 2 — Ravi row

| UI action | HTTP |
| --- | --- |
| Open Ravi | GET /api/customers/CUS-1002 |

## Step 3 — Status codes

| Code | When |
| --- | --- |
| 200 | list or single get succeeds |
| 201 | create succeeds |
| 400 | validation fails, blank name or bad email |
| 404 | unknown id, CUS-9999 |
| 500 | server fault, nothing the user did |

## Step 4 — JSON shape

    { "customerId": "CUS-1001", "name": "Amina Khan", "status": "ACTIVE" }

list is an array of these. customerId, name, status.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab35-api.md`
- [ x ] Table + Ravi row
- [ x ] Five status codes
- [ x ] JSON fields listed
