Module 13: Lab 13 contract-first mindset (exercise 4)

why northstar's soap starts from the contract rather than from the java I
already have. sources are the slides and lab 12's CustomerService, which is the
code-first version of this service and makes the argument concrete.


DEFINITION

Contract-first means defining the types and operations in XSD and WSDL first,
then generating or writing the Java to match.

the other direction is code-first, java classes exist and a tool derives the
contract from them. the slides call both valid and say to pick on the
integration's needs, so this is a choice with a reason, not a rule.


WHY THIS ONE IS CONTRACT-FIRST

the partner is a regional billing partner outside northstar. the slides put
contract-first as best for shared external contracts and long-lived
integrations, code-first as best for internal or simple services and
prototypes, and this is the first case on both counts. the partner integrates
against a document they hold, and the whole point of freezing it now is that
lab 24 can build behind it without renegotiating.

the second reason is control. contract-first gives strong control over the xml
and the namespaces. going the other way, the xml is whatever the tool decides my
java means, and I find out what the partner sees after the fact.


RISK 1, ACCIDENTAL BREAKING CHANGES

with code-first the contract is a shadow of the class, so any edit to the class
is an edit to the contract. renaming a field, reordering it, changing a type,
none of those look like partner-facing changes in an IDE and all of them are.

lab 12 shows exactly this. I renamed doStuff to createCustomer, getCustomer and
updateStatus while refactoring, which was a good change to the java and would
have been three operation renames on a published wsdl. the refactor was safe
because nothing outside the project called it. the moment a partner generates a
client, that same refactor is a breaking release.

the slides' versioning strategy is the fix, bump the target namespace for
breaking changes, add new fields as optional, keep old versions live until
consumers migrate. that only works if the contract is the thing you edit
deliberately. code-first makes the contract move on its own.


RISK 2, FRAMEWORK LEAKAGE INTO THE CONTRACT

code-first publishes whatever the class carries, including things that are about
java rather than about customers. lab 12 has candidates sitting right there. the
DTO stubs create and getById that still throw UnsupportedOperationException from
lab 8, and equals and hashCode built on customerId. none of those mean anything
to a billing partner, and a generator does not know which fields are the domain
and which are scaffolding.

the class comment in Customer.java makes the same point from the other side, it
says any @Entity or jakarta.persistence suggestion is rejected. that is me
keeping a persistence framework out of the domain type. contract-first is the
same instinct applied to the wire, the partner's view of a customer should not
depend on which framework happens to be on my classpath this month.


CANDIDATE OPS, PAPER NAMES ONLY

| Operation | What it does | Where it stands |
| --- | --- | --- |
| GetCustomer | fetch one customer by id | on the lab's list, maps to lab 12 getCustomer |
| ActivateCustomer | move a customer to ACTIVE | paper name, exercise 2 keeps it as UpdateCustomer with a status |

names on paper, no xsd and no wsdl written. the lab slide's own operation list
is CreateCustomer, UpdateCustomer, GetCustomer, so ActivateCustomer stays a
candidate rather than a commitment, same conclusion as exercise 2.


BOUNDARY

pre-lab only, do not finish the full graded lab in this exercise. no xsd, no
wsdl, no generated java. spring-ws hosting is lab 24.


SELF-CHECK

| # | Confirm | Result |
| --- | --- | --- |
| 1 | File exists, lab13-contract-first.md | Pass, under notes\Week 2\Module 13 |
| 2 | Contract-first sentence written | Pass, one sentence under DEFINITION |
| 3 | Two code-first risks | Pass, breaking changes and framework leakage, both with a lab 12 example |
| 4 | Two operations listed | Pass, GetCustomer and ActivateCustomer, paper names |
