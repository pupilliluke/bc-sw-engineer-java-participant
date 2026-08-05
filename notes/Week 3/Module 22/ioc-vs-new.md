# Lab 22 — IoC Versus Manual Wiring

| Approach | Who creates collaborators? | Test impact |
| --- | --- | --- |
| Manual `new` | Service constructs InMemoryCustomerRepository | Hard to swap fakes |
| IoC / DI | Spring (or test) supplies collaborators | Constructor takes a fake repo|

## Smell (one sentence)
CustomerService owns `new InMemoryCustomerRepository()`.


## Fix (one sentence)
Inject the repository through the constructor.

## Scope
Pre-lab only.


Rewrite a service that news NotificationService so a unit test can assert notify was called without Spring.

Take NotificationService as a constructor parameter instead of newing it. The
test then passes a subclass that records the id it was called with, and asserts
on the recording. Nothing in the test needs a context.

  new CustomerService(new InMemoryCustomerRepository(), recordingNotifier)


If two places each `new` a repository, how many in-memory maps exist at runtime?

Two. Each constructor builds its own ConcurrentHashMap, so a save through one
is invisible to the other and the seeded fixtures are duplicated rather than
shared.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/ioc-vs-new.md`
- [ x ] Table filled
- [ x ] Smell noted
- [ x ] Fix noted
