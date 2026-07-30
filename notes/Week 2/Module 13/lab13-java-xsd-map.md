Module 13: Lab 13 java to xsd map (exercise 3)

lab 12's Customer mapped to xsd types, amina and ravi as the two row sets. the
class has six fields, the exercise's sample map shows three, so this does all
six and says which ones I am unsure about.


THE MAP

| Java field | XSD idea | Amina CUS-1001 | Ravi CUS-1002 |
| --- | --- | --- | --- |
| String customerId | xsd:string, required | CUS-1001 | CUS-1002 |
| String fullName | xsd:string, required | Amina Khan | Ravi Singh |
| String email | xsd:string, minOccurs="0" | amina.khan@example.com | ravi.singh@example.com |
| String phone | xsd:string, minOccurs="0" | 555-0101 | 555-0102 |
| CustomerStatus status | simpleType, enumeration of four | ACTIVE | PROSPECT |
| LocalDateTime createdAt | a date type, see below | set by the service | set by the service |

values come from lab 12's Main, which creates both customers.


OPTIONALITY COPIES LAB 12, NOT MY GUESS

the slides say optionality and cardinality should be intentional, so each
minOccurs is read off what createCustomer actually enforces. requireNonBlank is
called on customerId and fullName, and on nothing else. email and phone can be
null and the customer is still created.

so email and phone are minOccurs="0" in the map. making either one required in
the schema would mean the contract rejects a customer my own service is happy
to build, and the partner would hit that before any java ran.

phone is the field the exercise expects to be optional anyway. email being
optional is a lab 12 finding, and it is worth a second look during the lab,
because a CRM customer with no email is arguably a validation gap rather than a
schema decision.


STATUS IS AN ENUMERATION, NOT A STRING

the exercise's sample row offers "xsd:string or enum". CustomerStatus is a java
enum with exactly four constants, and the slides list enumerations under
constraining values, so the schema should restrict it the same way. mapping it
to a plain xsd:string throws away the only guarantee the java type gives.

one thing the enumeration cannot carry. createCustomer maps a null status to
PROSPECT, that default lives in the service and nothing in the schema says so. a
partner who omits status cannot tell what they will get back.


CREATEDAT IS THE ONE I AM NOT SURE ABOUT

five fields map onto types the slides covered. createdAt does not.
LocalDateTime carries a date and a time and no zone or offset, so a value that
crosses a partner boundary means 17:00 somewhere without saying where.

xsd has date and time types and I have not used them yet, so the specific one
and whether it wants an offset is a lab question. what I can already say is that
the java type is the weak end. the service sets it with LocalDateTime.now(),
which also means every run produces a different value, so sample envelopes will
need a fixed timestamp rather than whatever lab 12 prints.

flagging it rather than guessing. the slides' rule is choose correct data types,
and I do not know this one yet.


THE ID PATTERN

step 2. CUS-#### as documentation, four digits, the shape every fixture from
lab 8 onwards uses. the slides list pattern restrictions alongside enumerations,
so the schema could enforce it later.

leaving it unenforced for now, for two reasons. lab 12 checks non-blank and
nothing else, so a pattern in the schema would be stricter than the code behind
it. and the slides say to keep backward compatibility in mind, which cuts
against locking a format before I know whether ids stay four digits.

CUS-9999 fits the pattern. that is why exercise 1's fault is a not-found rather
than a validation failure, the id is well formed and simply does not exist. an
id like CUSTOMER-1 would be the other kind, which is the second fault sample the
lab asks for.


THIS IS A MAP ON PAPER, NOT GENERATED JAXB

nothing here was generated and nothing here is compiled against. the class
already looks jaxb friendly, a no-arg constructor and getters and setters on all
six fields, which makes it easy to assume the mapping is automatic. it is a
table I wrote.

hosting and codegen with spring-ws is lab 24, not this prep.


BOUNDARY

pre-lab only. no xsd on disk, no generation, nothing to validate against yet.


SELF-CHECK

| # | Confirm | Result |
| --- | --- | --- |
| 1 | File exists, lab13-java-xsd-map.md | Pass, under notes\Week 2\Module 13 |
| 2 | Table includes both customers | Pass, amina and ravi across all six fields |
| 3 | Id pattern proposed | Pass, CUS-#### as documentation, deliberately not enforced |
| 4 | Lab 24 hosting deferred | Pass, with codegen alongside it |
| 5 | Fixtures match | Pass, CUS-1001 ACTIVE, CUS-1002 PROSPECT |
