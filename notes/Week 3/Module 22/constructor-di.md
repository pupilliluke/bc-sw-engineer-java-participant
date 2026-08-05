# Lab 22 — Constructor Injection Preference

## Preferred pattern
constructor with final CustomerRepository + NotificationService.

## Why (testability)
required deps explicit; unit test = new CustomerService(fakeRepo, fakeNotifier).

## Avoid
field @Autowired as primary pattern.

## Setter role (one line)
optional only — not Lab 22 primary wiring.

## Scope
Pre-lab only.


Can CustomerRepository be final if injected only via field @Autowired?

No. A final field is assigned at declaration or in the constructor and never
again. Field injection runs after the constructor, so by then the field is
either already assigned and cannot be reassigned, or never assigned and the
class did not compile.

What happens at startup if a constructor dependency bean is missing?

A BeanCreationException is thrown ( NoSuchBeanDefinitionException) , indicating that the required dependency is not available.


- [ x ] File exists at `notes/constructor-di.md`
- [ x ] Preferred pattern
- [ x ] Avoid noted
- [ x ] Testability noted