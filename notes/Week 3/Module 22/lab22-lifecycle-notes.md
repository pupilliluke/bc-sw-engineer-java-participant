# Lab 22 — Bean Lifecycle Callbacks

## Lifecycle order
Definition Loaded
Instantiation
Populate properties (dependency injection)
@PostConstruct callback
Bean is ready to use
@PreDestroy callback

## @PostConstruct purpose
Setup after the bean is created and injected

## @PreDestroy purpose
Cleanup before bean destroyed

## What not to do in init
Do not execute long-running or blocking tasks
Do not expect all other Spring beans to be fully ready
Do not execute initialization logic inside the constructor
Do not trigger circular dependencies

If @PostConstruct runs before constructor injection finishes, is that possible in Spring?

no


How many @PostConstruct logs do you expect for a singleton CustomerService per SpringBootTest context?

One. The bean is created once per context and the callback runs on creation.
A second line means a second context, not a second request.


## Anti-pattern
Initialization work in the constructor, before the injected collaborators are
usable. Use @PostConstruct to log startup/shutdown of CRM services.


- [ x ] File exists at `notes/lab22-lifecycle-notes.md`
- [ x ] Order stated
- [ x ] Both callbacks
- [ x ] Anti-pattern noted