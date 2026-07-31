Module 16: Lab 16 catch order (exercise 1)


STEP 1, LIST TYPES

NotFoundException, ConflictException, ValidationException, Exception.


STEP 2, ORDER

top to bottom, specific first:

    1) NotFoundException
    2) ConflictException
    3) ValidationException
    4) Exception, fallback, last


STEP 3, WHY

a broad catch first would shadow the specific domain mapping below it.


STEP 4, PREP ONLY

do not complete full lab 16 advice wiring in pre-lab.


SCOPE

pre-lab only, do not finish the full graded lab in this exercise.


SELF-CHECK

fixtures confirmed, Amina CUS-1001 ACTIVE, Ravi CUS-1002 PROSPECT, correlation
lab-request-001.

| # | Confirm | Result |
| --- | --- | --- |
| 1 | File exists, lab16-catch-order.md | Pass |
| 2 | Four types ordered | Pass, under STEP 2 |
| 3 | Rationale sentence present | Pass, under STEP 3 |
| 4 | Pre-lab boundary present | Pass, under STEP 4 |
