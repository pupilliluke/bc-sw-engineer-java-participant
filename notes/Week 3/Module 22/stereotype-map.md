# Lab 22 — Stereotype Annotation Map

| Class | Stereotype |
| --- | --- |
| CustomerController | @RestController |
| CustomerService | @Service |
| NotificationService | @Service |
| InMemoryCustomerRepository | @Repository |
| Customer (model) | none — plain type |

## Scope
Pre-lab only.


Should CustomerRepository (the interface) get @Repository?

No. The stereotype marks the class Spring instantiates, and the interface is
never instantiated. It is the injection type on the constructor parameter, so
one annotated implementation is enough to satisfy it.


What exception do you see if InMemoryCustomerRepository lacks @Repository and no @Bean?

NoSuchBeanDefinitionException for CustomerRepository, wrapped in
UnsatisfiedDependencyException naming constructor parameter 0. It fires at
context refresh, not on the first request.


- [ x ] File exists at `notes/stereotype-map.md`
- [ x ] Five rows filled
- [ x ] Model is plain
- [ x ] Controller is RestController
