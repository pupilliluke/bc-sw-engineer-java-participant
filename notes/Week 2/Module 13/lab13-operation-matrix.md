Module 13: Lab 13 operation matrix (exercise 2)

in, out and fault for the two operations the exercise names. sources are the
slides, the exercise text, and lab 12's CustomerService for what these calls
already do in java.


THE MATRIX

| Operation | In | Out | Fault |
| --- | --- | --- | --- |
| GetCustomer | customerId, correlationId optional | id, name, status | not found, soap:Client, CUSTOMER_NOT_FOUND |
| ActivateCustomer | customerId, target status, correlationId optional | new status | invalid transition |

design only. neither is written as xml yet.


GETCUSTOMER

in is one required field, the id. correlationId comes along optional, the slides
put correlation ids under safe diagnostic context and lab 12 already stamps
every failure with one, so the wire should be able to carry it.

out, the exercise says id, name, status. lab 12's Customer holds six fields and
toString prints those same three, so the exercise's shorthand and my own class
agree on what a customer looks like at a glance. whether the response returns
three fields or all six is a schema question, exercise 3.

fault is the one from exercise 1, Client with CUSTOMER_NOT_FOUND in detail,
CUS-9999 as the id that was never created.


ACTIVATECUSTOMER IS NOT ONE OF THE THREE

the lab overview slide lists the operations as CreateCustomer, UpdateCustomer,
GetCustomer, and the evaluation criteria name the same three. ActivateCustomer
shows up only on the exercise slides, marked design only on exercise 2 and put
under candidate ops, paper names only, on exercise 4. that phrase covers
GetCustomer too, so it means neither is written as xml yet rather than that
ActivateCustomer is excluded. the lab's own operation list is what excludes it.
so the matrix is asking me to design an operation the lab is not going to build.

in lab 12 that call already exists and it is not called activate either, it is
updateStatus(customerId, CustomerStatus.ACTIVE), one method that takes any
target status. Main's line is literally labelled activate 1002 and calls
updateStatus. so activation is a status change with a specific argument.

the choice for the contract is whether to publish it as its own operation or as
UpdateCustomer carrying a status. UpdateCustomer is already on the list, and the
slides say to add new fields as optional to stay backward compatible and to bump
the namespace for breaking changes. a fourth operation is a new portType entry
with its own messages and binding, which partners have to regenerate against. a
status field on an operation that already exists costs nothing on their side.

the argument the other way is intent. ActivateCustomer in the portType tells a
partner what the call means, UpdateCustomer makes them learn that status ACTIVE
is the activation. worth paying for once activation grows rules of its own, a
notification, an audit trail, a different permission. lab 12's updateStatus has
none of that, it sets the field and returns.

decision for now: keep it as UpdateCustomer with a status, list ActivateCustomer
as a paper name.


INVALID TRANSITION IS A RULE NOBODY ENFORCES YET

the exercise gives ActivateCustomer the fault "invalid transition". worth being
clear what that is.

CustomerStatus has four constants, PROSPECT, ACTIVE, SUSPENDED, CLOSED, so a
status outside that set is already impossible, the enum stops it in java and an
enumeration would stop it in the schema. CLOSED to ACTIVE is a different thing,
every value involved is legal and the question is whether the move is allowed.

lab 12 does not answer it. updateStatus takes newStatus, checks it is not null,
looks the customer up and calls setStatus. there is no transition table
anywhere in my code. so invalid transition is a fault I can document and cannot
raise. the slides list business faults, not-found, invalid status, duplicate,
validation, as things the contract should define, so writing it down now is the
job, implementing it is lab 24's.


HAPPY PATH

step 3. ravi, CUS-1002, PROSPECT to ACTIVE, correlationId lab-request-001. same
path lab 12's Main demos on the activate 1002 line.

amina is already ACTIVE, which is why ravi is the one who moves. running
activate on amina would prove nothing, the status is the same before and after.


BOUNDARY

Design only, do not complete full Lab 13 build.

pre-lab only. no xsd, no wsdl, no samples on disk.


SELF-CHECK

| # | Confirm | Result |
| --- | --- | --- |
| 1 | File exists, lab13-operation-matrix.md | Pass, under notes\Week 2\Module 13 |
| 2 | Both operations have in, out and fault | Pass, in the matrix table |
| 3 | Ravi activate path noted | Pass, CUS-1002 PROSPECT to ACTIVE |
| 4 | Design-only boundary present | Pass, under BOUNDARY |
| 5 | Fixtures match | Pass, CUS-1001 ACTIVE, CUS-1002 PROSPECT, lab-request-001 |
