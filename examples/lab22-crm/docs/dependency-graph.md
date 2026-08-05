# Lab 22 — Dependency graph

## Bean edges

- `CrmApplication` scans `com.northstar.crm`
- `CustomerController` (`@RestController`) → `CustomerService`, `CustomerMetrics`
- `CustomerService` (`@Service`) → `CustomerRepository` (`InMemoryCustomerRepository`, `@Repository`)
- `CustomerService` → `CustomerMetrics` (`@Component`)
- `CustomerService` → `NotificationService` (`@Service`)
- `CustomerMetrics` → `MeterRegistry` (Boot autoconfiguration)
- `CrmReadinessIndicator` (`@Component`), `CorrelationFilter` (`@Component`) — no CRM collaborators

All constructor-injected, all `final`, all default singleton scope.

`CustomerMetrics` is carried over from lab 21 and is not in the lab 22 starter.
It is the third constructor parameter on `CustomerService` and the second on
`CustomerController`, so it belongs in the graph.

## Fixtures

- `CUS-1001` Amina Khan ACTIVE
- `CUS-1002` Ravi Singh PROSPECT
- Correlation: `lab-request-001`, default from `CorrelationFilter` when the
  `X-Correlation-Id` header is absent

## Why constructor injection

The dependencies are required, so the constructor is where they belong: a
`CustomerService` cannot exist without a repository and a notifier, and `final`
fields make that a compile-time rule rather than a startup surprise. Field
`@Autowired` cannot assign a `final` field, and it hides the dependency list
from anyone reading the class. `CustomerServiceTest` constructs the service with
`new` and no container, which is only possible because the constructor takes
everything it needs.

## Anti-pattern

`new InMemoryCustomerRepository()` inside `CustomerService`. Experiment 5 in
`notes/screenshots/lab-22/03-failure-experiments.txt` has the measurement: the
suite stays green and the service and the injected bean hold separate stores.
