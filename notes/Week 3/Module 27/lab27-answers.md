Lab 27 transaction management (reflection questions, checkpoints)

built under examples\lab27-crm, copied from the lab 27 starter. JPA and H2
replace the in-memory maps, @Transactional sits on TransferService.transfer and
nowhere else, and ACC-FORCE-FAIL proves the rollback. 2 tests green on two
consecutive clean runs. app captured on port 8080, all five experiments run.


REFLECTION QUESTIONS

1. Which design decision most affected correctness (transaction boundary size)?

keeping debit, credit and the log inside one method. they are three writes and
one unit of work, and the boundary is the only thing that makes them one. if the
controller had called a debit method then a credit method, each would have
crossed the proxy separately and got its own transaction, so a failure between
them leaves the debit committed. same three writes, no atomicity.

2. What evidence proves rollback works?

the SQL log more than the balances. the failed attempt issues one select and no
update at all, because hibernate flushes at commit and the rollback came first,
so the debit never reached the database. forceFailRollsBack asserts the same
thing from the other side, balance equal to before and log count unchanged.

3. Which failure was hardest (proxy / self-invocation / exception type)?

self-invocation, experiment 5, and it is not close. it does not throw anything
different, it does not log a warning and the code reads correctly. the only
signal is 10.00 missing from MAIN afterwards. what made it worse than expected
is that SimpleJpaRepository.save is transactional on its own, so removing the
outer boundary does not leave the writes unmanaged, it gives each one its own
commit. the failure needs both facts to explain and neither is visible at the
call site.


CHECKPOINTS

| Checkpoint | Confirm | Result |
| --- | --- | --- |
| A1 | lab27-crm under examples/ | Pass, copied from starter/ |
| A2 | JPA + H2 with jdbc:h2:mem:lab27 | Pass, ddl-auto update, show-sql on |
| A3 | Account fields id / customerId / type / balance | Pass, no status field |
| B1 | Seeds ACC-MAIN-1001 + ACC-LOYALTY-1001 only | Pass, 1000.00 and 50.00, ACC-FORCE-FAIL not persisted |
| B2 | @Transactional TransferService + TransactionLog | Pass, annotation on the service method only |
| B3 | Happy MAIN to LOYALTY, HTTP 200 {"status":"OK"} | Pass, 50.00 moved |
| C1 | ACC-FORCE-FAIL, HTTP 500, MAIN unchanged, no success log | Pass, and no update statement was issued at all |
| C2 | ACID table cites observations in docs/acid-notes.md | Pass, isolation marked as not demonstrated |
| C3 | Tests run: 2 | Pass, forceFailRollsBack and happyPathMovesFunds |
| D1 | mvn test green | Pass, two consecutive clean runs |
| D2 | README / notes runbook complete | Pass |
| D3 | No secrets / target/ committed | Pass, target/ ignored, H2 sa with blank password is lab-only |

FULL PATH

| Item | Result |
| --- | --- |
| All five failure experiments | Pass |
| Self-invocation measured, not described | Pass, before=1000.00 after=990.00 lost=10.00 |
| AI review notes | Pass, README, four rejects and two accepts |
| Insufficient-funds check | not added, the guide states the solution does not implement one |

SECURITY AND PRODUCTION REVIEW

1. which inputs are untrusted?

the whole JSON body. fromAccountId, toAccountId and amount are caller supplied
and amount goes straight into new BigDecimal, so a non-numeric string is a 500
rather than a 400. the only rule is that amount must be positive.

2. where are authn/authz/validation enforced?

nowhere. the transfer route is open, so anyone who can reach the port can move
money between the seeds. lab 28 is the security pass.

3. which values are sensitive, and where stored?

balances and the transaction log, both in H2 in-memory and both fictional. the
datasource is sa with a blank password, which is lab-only and never a real
configuration.

WINDOWS HOW-TO PASS CRITERIA

| # | Confirm | Result |
| - | --- | --- |
| 1 | workspace open in IntelliJ with SDK 21 | Pass, temurin-21.0.4 |
| 2 | lab project under examples/lab27-crm | Pass |
| 3 | GUIDE deliverables and checkpoints complete | Pass, full path |
| 4 | commands succeed | Pass, mvn -B test and spring-boot:run |
| 5 | evidence under notes/screenshots/lab-27 | Pass, kept in the project as since lab 14 |
