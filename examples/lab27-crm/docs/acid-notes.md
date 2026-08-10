Lab 27 ACID evidence

Every row cites something in notes/screenshots/lab-27/, not a definition.

| Property | Lab evidence |
| -------- | ------------ |
| Atomicity | ACC-FORCE-FAIL returns HTTP 500 and MAIN is unchanged. forceFailRollsBack reads the balance before and after and they compare equal, and the log count is unchanged. the debit and the credit are one unit or neither happens |
| Consistency | after the happy path MAIN is down 50.00, LOYALTY is up 50.00 and exactly one transaction_log row was inserted. the pair of balances and the log agree, and no failed attempt left a row |
| Isolation | default isolation, not demonstrated under concurrency. one request at a time in this lab, so nothing here proves what a second concurrent transfer would read mid-flight |
| Durability | H2 in-memory with DB_CLOSE_DELAY=-1, so committed data survives only while the JVM runs. a restart drops everything and AccountSeed writes the seeds again. real durability needs a file-backed or PostgreSQL datasource, which is week 4 |

WHAT THE SQL LOG SHOWS

show-sql is on, so the statements are visible. The happy path issues two selects,
an insert into transaction_log and two account updates. The forced failure issues
one select and nothing else.

No update is sent at all on the failed attempt. Hibernate defers the write to the
flush at commit, and the transaction rolled back before the flush, so the debit
never reached the database. accountRepository.save on a managed entity does not
issue SQL where it is called.

That makes rollback here stronger than an undo. There was nothing to undo.

WHAT THE EVIDENCE DOES NOT COVER

Isolation is the honest gap. Nothing in this lab runs two transfers at once, so
the isolation row is a statement about configuration and not an observation.

Durability is bounded by H2 in-memory. A committed transfer survives a second
request, not a restart.

Account has id, customerId, type and balance and no status field, so there are no
lifecycle rules to cite here.
