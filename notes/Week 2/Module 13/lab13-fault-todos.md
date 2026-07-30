Module 13: Lab 13 fault envelope todos (exercise 1)

the six blanks filled. sources are the slides, the exercise text, and lab 12's
CustomerService, which already throws the not-found this fault has to carry.


FAULT TODOS

Fault code: soap:Client

Fault string:

    Customer not found: CUS-9999 correlationId=lab-request-001

Detail customerId: CUS-9999

Correlation id: lab-request-001

HTTP/transport note (placeholder): none claimed. the contract and the sample
envelopes exist, nothing hosts them, so there is no address to call and no
status code to observe. the slides put a server fault at often 500 and say the
fault payload is the primary error contract while http status varies by version
and framework.

Real hosting lab: Lab 24, spring-ws.


CLIENT AND NOTFOUND ARE TWO DIFFERENT SLOTS

the exercise asks for a Client/NotFound style code. the slides give faultcode as
Client or Server, so NotFound is not a faultcode value. Client is the right half,
it says the sender caused this and sending it again gets the same answer, which
is true of asking for an id that was never created. Server would invite a retry
that fails forever.

the notfound half goes in detail as CUSTOMER_NOT_FOUND, which is where a
partner's code branches. faultstring is prose for a human, detail is the machine
readable copy. the exercise also asks for detail customerId, so the id goes in
beside the code. it is already in the faultstring, but reading it out of a
sentence means parsing english, and the sentence is the thing most likely to get
reworded.


THE FAULTSTRING IS LAB 12'S MESSAGE

not invented for this exercise. CustomerService.requireExisting already throws

    "Customer not found: " + customerId + " correlationId=" + correlationId

so with CUS-9999 and the default correlation id that is exactly the string
above. reusing it means the fault a partner reads and the exception the service
throws say the same thing, and lab 24 has one less translation to write.


WHY CUS-9999 AND NOT AMINA

CUS-1001 and CUS-1002 are the create, get and update fixtures, they have to
resolve. reusing one as the not-found example means the same id is present in one
envelope and missing in the next, and a partner reading the samples cannot tell
which is the contract.

CUS-9999 exists only to be absent. lab 12's Main already uses it that way, the
last call is getCustomer("CUS-9999") landing in the caught unknown-id branch, so
the id has never been created anywhere in my code.


CORRELATION IN THE FAULT

lab-request-001, the same value as module 12 exercise 5 and the same constant
lab 12 concatenates into every failure message. it goes in the faultstring
because a fault has to carry its own copy, the request that caused it is gone by
then.

that is the point of putting it there. a partner pastes the error into a ticket,
support greps the same string, no timestamp hunt.

never in the fault: email, phone, fullName. same rule as module 12 exercise 5,
and the slides say the same under logging, log correlation ids and safe
diagnostic context, redact or avoid sensitive message content. a fault is the
message most likely to end up in a ticket or a screenshot.


BOUNDARY

Placeholder endpoint only, no Spring-WS hosting in Lab 13 prep.

pre-lab only. no wsdl, no xsd, nothing hosted.


SELF-CHECK

| # | Confirm | Result |
| --- | --- | --- |
| 1 | All _____ replaced | Pass, six of six |
| 2 | CUS-9999 and lab-request-001 present | Pass, in the faultstring and again in the detail |
| 3 | Lab 24 named | Pass, as the hosting lab |
| 4 | CUS-9999 is the not-found example, not Amina or Ravi | Pass, CUS-1001 and CUS-1002 stay valid |
| 5 | Placeholder honesty sentence written | Pass, under BOUNDARY |
