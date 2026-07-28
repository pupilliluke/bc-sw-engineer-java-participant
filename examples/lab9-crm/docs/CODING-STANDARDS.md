Northstar CRM coding standards (Lab 8)

Short on purpose. If a rule can't be checked by reading imports or a file
listing, it doesn't belong here yet.

LAYERS

  controller   transport / API mapping only
  service      business rules and orchestration
  repository   persistence boundary, save/find
  entity       domain model
  dto          request/response contracts
  config       wiring
  exception    domain and API failures

HARD RULES

Dependencies point inward, never back out:

  controller -> service -> repository -> entity
  controller -> dto
  service    -> dto, entity, exception
  entity     -> nothing in other CRM layers

Services must not depend on controllers. Entities must not carry HTTP or SOAP
types. Repositories must not import controllers, and ideally not DTOs either.
Practical check: open any file under entity/ or repository/ and read the
imports. A com.northstar.crm.controller import means the rule is broken.

NAMING

  Classes      CustomerService, CustomerRepository, one responsibility per name
  Methods      findById, save, create, verb-first
  Customer ids CUS-#### in all examples and docs (CUS-1001 Amina Khan)
  Packages     lowercase, com.northstar.crm.<layer>

DTO VS ENTITY

CustomerRequest / CustomerResponse are the API contract, Customer is the domain
model. Same customer, different shapes, mapped in the service. Persistence
annotations stay on the entity when JPA arrives, request/response shapes never
grow storage concerns.

EXCEPTIONS

Domain failures get their own type carrying context, CustomerNotFoundException
holds the id it couldn't find. Services throw, the controller boundary catches
and maps. RuntimeException base for now, revisit when the API labs arrive.

DO NOT COMMIT

  target/ and IDE metadata (.idea/, *.iml)
  secrets, tokens, passwords, real JDBC URLs
  production customer PII, demo names only

TOOLING

  JDK 21, Maven 3.9+, maven.compiler.release=21
  IntelliJ with format-on-save, keep diffs clean
