# Lab 22 — Bean Graph Skeleton

## Edges (fill TODOs)
CustomerController → CustomerService
CustomerService → CustomerRepository
CustomerService → NotificationService
Optional metrics edge: CustomerService → CustomerMetrics, carried from lab 21

## Unit-test construction (one line)
new CustomerService(new InMemoryCustomerRepository(), metrics, new NotificationService())

## Scope
Pre-lab only.


If NotificationService also depended on CustomerService, what problem appears?

A constructor cycle. Neither bean can be built first, so the context fails at
refresh. The fix is to break the cycle, not to field-inject one side.


Does the graph include `new` edges inside CustomerService after IoC refactor?

No. Every collaborator arrives through the constructor, so the only edges left
are the injected ones.


- [ x ] File exists at `notes/bean-graph-sketch.md`
- [ x ] Controller→Service
- [ x ] Service→Repo
- [ x ] Service→Notifier
