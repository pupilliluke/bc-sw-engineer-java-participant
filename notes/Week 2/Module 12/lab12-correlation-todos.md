Module 12: Lab 12 correlation todos (exercise 5)

the five blanks from the exercise, filled. the activate path they describe is
CUS-1002 ravi, PROSPECT to ACTIVE, which through the target API in notes.md
exercise 1 is updateStatus("CUS-1002", CustomerStatus.ACTIVE).


CORRELATION TODOS

Correlation id value: lab-request-001

Log on activate entry:

    activate entry customerId=CUS-1002 correlationId=lab-request-001

Log on activate success for Ravi:

    activate ok customerId=CUS-1002 PROSPECT->ACTIVE correlationId=lab-request-001

Never log field: raw email. phone and fullName are the same class of data and
are equally out. customerId is the handle that goes in the log instead, it
identifies the record to anyone with access to the system and means nothing to
anyone without it.

Place correlation in: MDC.


WHY MDC AND NOT HEADER

a header is where a correlation id enters the system, MDC is where it lives
while the call runs. picking header would mean threading the value through
every method signature that might want to log, which is the thing MDC exists to
avoid.

honest caveat, neither exists in lab 12. there is no HTTP layer until SOAP in
lab 13, and no SLF4J or logback on the classpath, so there is no real MDC to
put anything in. what lab 12 actually does is the guide's line

    "Customer not found: " + customerId + " correlationId=" + correlationId()

a field or a parameter, appended to the message. MDC is the answer to "where
does this belong", the string concatenation is the answer to "what can i build
this week". recording both so the note is not describing infrastructure that
isn't there.

this also matches what module 10 ex 2 and module 11 ex 2 already concluded,
lab-request-001 stays off the Customer record and rides the log line, because
one customer appears in many requests and storing the last one on the entity is
just wrong data.


THE ONE-LINER RULE

Every public service entry logs correlation once.

once is the load-bearing word. the messy baseline prints four times in one call,
"bad", "dup", "ok " + id and "upd", which is print spaghetti rather than
logging, and the guide says so directly. three public methods, three entry
lines, plus the failure paths that carry the id into the exception message.


PII, THE FAILURE MODE THIS PREVENTS

the exercise's own if-it-fails table names it, logging full payloads instead of
ids. the shape to avoid

    log("created " + customer)          Fail

Customer.toString carries customerId, fullName and status today, and the entity
also holds email and phone. logging the object means whatever gets added to
toString later silently lands in the log file too, and log files get copied,
shipped to support and kept far longer than anyone plans. log the id, name the
fields deliberately, never the object.


SELF-CHECK

| # | Confirm | Result |
| --- | --- | --- |
| 1 | correlation blank is exactly lab-request-001 | Pass, read back character by character, not from memory |
| 2 | all five blanks replaced | Pass |
| 3 | one correlation scheme, not a second invented one | Pass, lab-request-001 everywhere, no request-id or traceId variants |
| 4 | PII field named | Pass, raw email, with phone and fullName flagged alongside |

pre-lab only, prepare for lab 12, do not complete the full refactor now.
