# Lab 21 — Cardinality Anti-Patterns

| Label                         | OK?         |
|-------------------------------|-------------|
| outcome=success               | failure     | yes |
| customerId=CUS-1001           | no          |
| correlationId=lab-request-001 | no-use logs |

## Where ids go
Ids go in logs/traces. Good metric: crm.customer.create with outcome tag.

## Good metric sketch
1.23 %

- [ x ] File exists at `notes/lab21-cardinality-antipatterns.md`
- [ x ] Three label rows
- [ x ] Ids placement noted
- [ x ] Good metric sketched
