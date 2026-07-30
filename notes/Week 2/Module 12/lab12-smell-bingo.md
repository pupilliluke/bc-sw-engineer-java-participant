Module 12: smell bingo (exercise 3)

card is the five the exercise names. played against the real messy baseline
rather than an imagined one, labs\Week 2 - Backend, AI Tools and Testing\
module-12\lab12\starter\src\main\java\com\northstar\crm\service\CustomerService.java,
line numbers below are that file. its own header says INTENTIONALLY MESSY, so
every hit is planted, the exercise is whether i can name what each one costs.


THE CARD

| # | Smell | Where | What it costs CUS-1001 / CUS-1002 |
| --- | --- | --- | --- |
| 1 | long method | doStuff, lines 13-50 | one method validates, scans for a duplicate, maps a status string, builds and stores the customer, then conditionally loops the list again and mutates it. changing amina's duplicate rule means editing the same method that decides ravi's status, and there is no way to exercise the create path without the update branch sitting in the same call |
| 2 | magic strings for ACTIVE / PROSPECT | lines 31-35, 43-44 | status arrives as a String and is matched by an else-if chain. hand it "Active" or "AKTIVE" for CUS-1002 and every branch misses, the final else sets PROSPECT, and the call returns a customer as if it worked. ravi stays a prospect after an activation request and nothing anywhere reports a problem |
| 3 | == on Strings | line 55 in get, and line 15 in doStuff | `x.getCustomerId() == id` compares references. getCustomer("CUS-1001") only matches when the caller happens to pass the very same String object, so a id read from a file, a socket or `new String("CUS-1001")` returns null and amina looks deleted. line 15's `a == ""` is the same bug in the blank check and the starter's own comment does not flag that one |
| 4 | mixed I/O in the domain | lines 16, 22, 38, 45 | the service prints "bad", "dup", "ok " + id and "upd" straight to stdout. a test can assert nothing about them without capturing System.out, and none of the four says which customer or which rule, so "bad" for a blank CUS-1001 name and "bad" for a blank id are indistinguishable in a log |
| 5 | unclear names | doStuff, data, params a b c d e | five String parameters in a row named a to e. email and phone are both String and adjacent, so swapping them at a call site compiles, runs, stores ravi with his phone in the email field, and no test or reviewer catches it from the signature. `data` says nothing about holding customers |


PRIORITY, THE TWO TO FIX FIRST

★ 3, == on Strings. this is the only one that is already producing wrong
answers rather than merely inviting them. the read path is broken for any id
that is not the identical object, which is most real callers, and it fails by
returning null rather than by throwing, so it reads as "customer not found"
instead of "lookup is broken". first fix, and it is one operator.

★ 2, magic strings for status. same reason, silent wrong data. a mistyped
status does not error, it defaults, so the corruption is a valid-looking
PROSPECT that nobody queries for. fixed by taking CustomerStatus at the API
instead of a String, which is the sketch in notes.md exercise 1 and deletes the
whole else-if chain.

why not the long method first. it is the biggest job on the card and the one
the lab is really about, but it is a restructure, not a bug. doStuff computes
the right answer for well-formed input; the two starred ones compute the wrong
answer for input that looks fine. bugs before shape.

the other two are readability. they cost review time and invite the swapped
email/phone, but neither corrupts a fixture on its own.


CARD SIZE vs THE LAB

five here, and docs\smells.md in the lab 12 starter wants at least eight
catalogued with a CRM impact each. the three the bingo card does not cover are
already visible in the same file and go into the lab's table:

| Smell | Where |
| --- | --- |
| raw types | line 11, `List data = new ArrayList()`, forcing a cast on every read |
| null as control flow | lines 17, 24, 59, three different failures all returning null |
| magic "UPDATE" behaviour | line 39, `b.contains("UPDATE")` turns a create into an update based on the customer's name |

that last one is the strangest thing in the file. a customer legitimately named
something containing UPDATE would trigger it, so the fixture data itself decides
which code path runs.


BOUNDARY

pre-lab only, prepare for lab 12, do not complete the full refactor now.


PASS CRITERIA

| # | Confirm | Notes |
| --- | --- | --- |
| 1 | Five smells listed | Pass |
| 2 | Fixture impact noted | Pass, per row |
| 3 | Two priorities starred | Pass, 3 and 2 |
