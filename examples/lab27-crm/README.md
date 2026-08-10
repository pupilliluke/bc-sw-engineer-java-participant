Northstar CRM build (Lab 27)

  mvn -B test
  mvn -B spring-boot:run

  curl -s -X POST http://localhost:8080/api/transfers \
    -H "Content-Type: application/json" \
    -H "X-Correlation-Id: lab-request-001" \
    -d '{"fromAccountId":"ACC-MAIN-1001","toAccountId":"ACC-LOYALTY-1001","amount":"50.00"}'
  # expect 200 {"status":"OK"}

  curl -s -i -X POST http://localhost:8080/api/transfers \
    -H "Content-Type: application/json" \
    -H "X-Correlation-Id: lab-request-001" \
    -d '{"fromAccountId":"ACC-MAIN-1001","toAccountId":"ACC-FORCE-FAIL","amount":"10.00"}'
  # expect HTTP 500; MAIN unchanged

  git status --short

Copied from the lab 27 starter. JPA and H2 replace the in-memory maps of the
earlier labs, so a transfer is a real unit of work that commits or rolls back.
AccountSeed loads ACC-MAIN-1001 MAIN 1000.00 and ACC-LOYALTY-1001 LOYALTY 50.00
for CUS-1001. ACC-FORCE-FAIL is never persisted; it is a destination string that
triggers the throw.

@Transactional sits on TransferService.transfer and nowhere else. The controller
maps a JSON body to three arguments and returns {"status":"OK"}. There is no
@ExceptionHandler in this lab, so the forced failure surfaces as the Boot default
500.

Step order inside the boundary is validate amount, load from, debit and save,
force-fail check, load to, credit and save, write the log. The debit happens
before the check on purpose. Checking first would mean nothing had been written
yet and the rollback would prove nothing.

TESTS

  mvn -B test    Tests run: 2

forceFailRollsBack reads the MAIN balance and the log count, calls the transfer
to ACC-FORCE-FAIL, then asserts both are unchanged. happyPathMovesFunds moves
5.00 and asserts MAIN down 5.00, LOYALTY up 5.00 and one more log row. Both read
their before values rather than hard-coding 1000.00, so they pass in either
order and do not depend on the seed being untouched.

AI REVIEW

Rejected: @Transactional on TransferController instead of the service, a
try/catch around debit and credit that logs and returns, REQUIRES_NEW on the log
write, and a helper that calls this.transfer inside the same class.

The swallowed catch reads as defensive code and it breaks atomicity, because
Spring rolls back on exceptions that leave the method and that one never does.
Experiment 5 measures what the self-invocation version costs.

Accepted after review: constructor injection of both repositories, and
IllegalStateException as the forced failure since unchecked exceptions trigger
rollback by default.

SECURITY NOTES

untrusted: the JSON body. fromAccountId, toAccountId and amount are all caller
supplied, and amount is parsed straight into BigDecimal, so a non-numeric string
is a 500 rather than a 400. the only validation is that amount is positive.

authn/authz: none. anyone who can reach the port can move money between the
seeded accounts. that is lab 28.

sensitive: balances and the transaction log. both are fictional here. H2 runs
with user sa and a blank password, which is lab-only and never a real
configuration.

There is no idempotency key, so a repeated request moves money twice.
Experiment 3 shows two identical posts both returning 200.

CLEANUP

  mvn -q clean
  git status --short

Ctrl+C spring-boot:run. target/ is ignored. Keep lab27-crm, lab 28 secures the
customer APIs.

NOTES

Evidence and the five failure experiments are in notes/screenshots/lab-27/.
Checkpoints and reflection answers are in notes/Week 3/Module 27/lab27-answers.md.
The ACID table is docs/acid-notes.md. Full GUIDE at
labs/Week 3 - Spring Framework and Enterprise Patterns/module-27/lab27/.
