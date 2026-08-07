# Lab 25 — Service Layer Skeleton

## Constructor deps
CustomerService(CustomerRepository repo). one dependency, constructor injection,
final field.

## create TODO
if repo.exists(id) throw IllegalStateException("duplicate"), else repo.save(id, name).
creating CUS-1001 twice is the case that has to fail.

## get TODO
find by id, throw not-found when it is missing. CUS-9999 is the missing one.

## Forbidden in this class
ResponseEntity, HttpStatus, @RequestBody, @RequestHeader, anything from spring
web. no map.put either, that call belongs to the repository.

## Scope
Pre-lab only.


## Debug / design challenge

Where should seeding of CUS-1001 live — service or repository?

repository. the InMemoryCustomerRepository constructor seeds CUS-1001 Amina Khan
and CUS-1002 Ravi Singh.

## Predict the Output / Behavior

Can create return ResponseEntity.ok(...) from the service?

no. that is the controller's type and it pulls http into the service.


## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab25-service-todo-skeleton.md`
- [ x ] Constructor deps
- [ x ] create/get TODOs
- [ x ] Forbidden HTTP noted
