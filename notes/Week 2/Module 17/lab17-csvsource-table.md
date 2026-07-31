Module 17: Lab 17 CsvSource table design (exercise 2)


REFERENCE

| inputStatus | valid? |
| --- | --- |
| ACTIVE | true |
| PROSPECT | true |
| ACTVE | false |
|  | false |

the blank row is written quoted in the annotation, '', so the case arrives as
an empty string rather than as null.


STEP 2, EXTRA ROW

| active | false |

lowercase active, because the status is parsed with CustomerStatus.valueOf and
that is case sensitive, so a client sending the right word in the wrong case is
rejected exactly like the ACTVE typo.


STEP 3, JDK/MAVEN

tests run on JDK 21 through Maven Surefire in the timed lab, each row reported
as its own case.


STEP 4, BOUNDARY

stubbing collaborators waits for lab 18.


SCOPE

pre-lab only, do not finish the full graded lab in this exercise.


SELF-CHECK

fixtures confirmed, Amina CUS-1001 ACTIVE, Ravi CUS-1002 PROSPECT, correlation
lab-request-001.

| # | Confirm | Result |
| --- | --- | --- |
| 1 | File exists, lab17-csvsource-table.md | Pass |
| 2 | Base rows present | Pass, under REFERENCE |
| 3 | Extra invalid added | Pass, lowercase active under STEP 2 |
| 4 | Lab 18 boundary noted | Pass, under STEP 4 |
