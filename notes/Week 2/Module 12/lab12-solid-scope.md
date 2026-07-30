Module 12: SOLID apply vs defer (exercise 2)

scope for the lab 12 refactor, one principle applied now and two written down as
deliberately deferred. the target API is the sketch in notes.md exercise 1,
createCustomer / getCustomer / updateStatus plus a private validation helper.


APPLY NOW, SRP

single responsibility. doStuff in the lab 12 starter does five jobs in one
method, validates the id and name, scans the list for a duplicate, maps a status
string through a chain of else-ifs, builds and stores the customer, and then, if
the name happens to contain "UPDATE", loops the list a second time and mutates
the status. five reasons for one method to change.

applied as the split in the sketch

| Job | Where it goes |
| --- | --- |
| create and store | createCustomer |
| read one | getCustomer |
| change status | updateStatus, and the "UPDATE" magic branch is deleted |
| blank id / blank name rule | private validateCustomerId helper |

the exercise names this "separate validation helper from persistence-shaped
code", and the persistence-shaped part is the raw List scan sitting inline in
the same method as the rules. splitting them means the validation rule can be
read in four lines instead of being spotted inside a for loop.

this one is cheap. it is method extraction inside a single class, no new types,
no new files, and lab 11 already did the same move with validateCustomerId, so
the shape is familiar and the risk is low.


DEFER 1, DIP

dependency inversion. the obvious next step after SRP is a CustomerRepository
interface with an in-memory implementation behind it, so the service depends on
an abstraction instead of on a List. not now.

it adds two types and a wiring decision to a lab whose graded deliverable is a
before/after refactor of one class. the guide's own forward-look question calls
the repository DIP the deferred step, and the lab 12 checkpoint asks for three
methods present, not for a persistence port. lab 8 already has a
CustomerRepository in the tree from earlier work, so when this does land it is a
reconciliation job, not a greenfield one, which is another reason not to start
it inside a refactor lab.

worth noting the guide does say to prefer a Map keyed by id over a List. that is
a data structure change inside the class, not inversion, and it does not need an
interface to happen.


DEFER 2, ISP

interface segregation. the case for it arrives with SOAP in lab 13, where a
generated port can carry a dozen operations and a client that wants one of them
is forced to see all twelve. splitting fat ports into narrow ones is a real
problem there.

here there is no interface at all to segregate. the service is one concrete
class with three public methods, and every caller uses all three. inventing
narrow interfaces now would be designing against a client that does not exist
yet, and the shape of the real one is decided by the wsdl in lab 13, not by me
guessing this week.


WHY DEFER

modules 10 to 12 stay before SOAP, so the ports the DIP and ISP work would be
built against do not exist yet and over-architecting them now means guessing at
a contract lab 13 defines.

the honest version of that sentence is that SRP pays off inside this lab and the
other two only pay off across a boundary the codebase does not have yet. a
deferred principle written down is a decision, the same one undocumented is just
something that did not get done, which is why this file exists rather than the
work being skipped silently.


BOUNDARY

pre-lab only, prepare for lab 12, do not complete the full refactor now.


PASS CRITERIA

| # | Confirm | Notes |
| --- | --- | --- |
| 1 | One apply-now item | Pass, SRP |
| 2 | Two defer items | Pass, DIP and ISP |
| 3 | Before-SOAP rationale written | Pass |
