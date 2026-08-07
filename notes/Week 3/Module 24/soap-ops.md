# Lab 24 — SOAP Operation Map

| SOAP operation | CustomerService method |
| --- | --- |
| CreateCustomer | create |
| GetCustomer | get |
| UpdateCustomerStatus | updateStatus |
| ListCustomers | list |

## Shared service?
yes, one CustomerService bean for REST and SOAP. the endpoint maps xml to java
and calls it, the rules stay in one place so the two protocols cannot fork.

fixtures: CUS-1001 Amina Khan ACTIVE, CUS-1002 Ravi Singh PROSPECT, CUS-9999
not-found. correlation lab24-001 on SOAP, lab-request-001 on REST.

## Scope
Pre-lab only.

## Debug / design challenge

What goes wrong if SOAP uses a second InMemoryCustomerRepository?

split data. a customer created over REST is missing over SOAP, and CUS-1001
answers differently depending on which protocol asked.

## Predict the Output / Behavior

Should GetCustomer re-validate business rules already in the service?

no. the endpoint maps and delegates, the service owns the rules.

## Pass criteria

Self-check before marking Pass:

- [x] File exists at `notes/soap-ops.md`
- [x] Four ops mapped
- [x] Shared service noted
