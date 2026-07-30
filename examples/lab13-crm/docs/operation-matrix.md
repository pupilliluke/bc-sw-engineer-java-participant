# Operation matrix — Lab 13

Scope table for the Northstar CRM customer contract. Three operations, frozen
before Lab 24 implements any of them.

| Operation | Purpose | Key inputs | Key outputs |
| --------- | ------- | ---------- | ----------- |
| CreateCustomer | Register a new CRM customer | fullName, email, phone?, status?, correlationId? | customer (with server-assigned ID) |
| UpdateCustomer | Change mutable fields / status | customerId, fullName?, email?, phone?, status?, correlationId? | customer |
| GetCustomer | Fetch one customer by ID | customerId, correlationId? | customer |

`?` marks `minOccurs="0"` in `contracts/customer.xsd`.

## Faults per operation

| Operation | Not found | Validation | Notes |
| --------- | --------- | ---------- | ----- |
| CreateCustomer | n/a | `fault-validation.xml` | blank `fullName`; duplicate ID is a service rule, not schema |
| UpdateCustomer | `fault-customerNotFound.xml` | `fault-validation.xml` | invalid status transition is a service rule (see design notes) |
| GetCustomer | `fault-customerNotFound.xml` | `fault-validation.xml` | blank `customerId` is schema-valid; see Experiment 2 |

## Out of scope

| Not in this contract | Why |
| -------------------- | --- |
| DeleteCustomer | no delete path in Labs 10–12; would need a retention decision first |
| ListCustomers / search | needs paging and filter design; no partner requirement yet |
| ActivateCustomer | activation is `UpdateCustomer` with `status=ACTIVE`; a fourth operation buys intent and costs a partner regeneration |

## Fixtures

| Item | Value |
| ---- | ----- |
| CUS-1001 | Amina Khan — ACTIVE |
| CUS-1002 | Ravi Singh — PROSPECT |
| CUS-9999 | never created — the not-found fixture |
| Correlation ID | `lab-request-001` |
| Namespace | `http://northstar.com/crm/customer` |
| Endpoint placeholder | `http://localhost:8080/ws` (**not live** in this lab) |
