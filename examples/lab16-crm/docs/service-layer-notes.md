Service layer notes (Lab 15)

WHAT MOVED, AND WHY

Lab 14's CustomerService did three jobs. It held a HashMap, it decided what was
legal, and it orchestrated the two. Lab 15 splits those across three objects.

| Job | Lab 14 | Lab 15 |
| --- | --- | --- |
| Hold state | CustomerService field | InMemoryCustomerRepository |
| Decide legality | if-statements in createCustomer | CustomerValidator |
| Orchestrate | CustomerService | DefaultCustomerService |
| Name the use cases | concrete class | CustomerService interface |

Storage is tested without any rule, the rules without touching the API, and the
facade against a mock of the interface. Twenty-two tests became forty.

THE REPOSITORY BOUNDARY

The repository saves state. It does not decide PROSPECT to ACTIVE.

The anti-pattern is a method called repo.activateCustomer(id). It reads as a
convenience and it puts a business rule in the layer that JPA replaces, where
the rule has to be rewritten or is lost. The correct split is findById, then the
transition decision in the service, then save.

existsByEmail is the call that looks like a rule and is not. It answers a
question about stored data. The decision that a duplicate is fatal is made in
CustomerValidator.

TRANSITION TABLE

  PROSPECT  -> ACTIVE, CLOSED
  ACTIVE    -> SUSPENDED, CLOSED
  SUSPENDED -> ACTIVE, CLOSED
  CLOSED    -> (none)

Held as a static EnumMap of EnumSet in CustomerValidator. Anything absent is
illegal, so there is no separate rejection list to drift out of sync with.

Same status is rejected, see the README for the reasoning and the cost.

TWO KINDS OF INVALID

| Kind | Example | Exception | Checked with |
| --- | --- | --- | --- |
| Malformed | blank customerId, bad email shape | IllegalArgumentException | the payload alone |
| Conflicting | duplicate id, duplicate email | IllegalStateException | the current store |
| Illegal move | ACTIVE to PROSPECT | IllegalStateException | the current status |

The first row is mostly Lab 14's annotations; the validator repeats the blank
checks as a backstop for callers that reach the service directly. The other two
rows cannot be expressed as an annotation.

Lab 16 replaces these JDK exception types with domain types. The facade already
carries the codes those types will need, CUSTOMER_CONFLICT and
CUSTOMER_TRANSITION_INVALID. Failure experiment 1 shows why the types are
needed, a repository outage arriving as IllegalStateException is currently
reported to the client as a duplicate.

VALIDATE BEFORE MUTATE

changeStatus reads, validates, then writes. Reversing the last two lines leaves
a rejected request with a corrupted stored status, and nothing rolls back an
in-memory object. There is no transaction here, and the same code runs under
@Transactional in a later lab.

SPRING PREVIEW

Today, in Main:

  CustomerRepository repo = new InMemoryCustomerRepository();
  CustomerValidator validator = new CustomerValidator(repo);
  CustomerService service = new DefaultCustomerService(repo, validator);

Later, the same constructors with @Service and @Repository on the classes.
Spring calls new instead of Main. The constructor parameter lists are unchanged.

Field injection would be shorter and would not allow this. The constructors are
what let Labs 17 and 18 build the graph without a container.
