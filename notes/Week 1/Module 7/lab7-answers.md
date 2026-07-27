Lab 7 ATM System (concepts to discuss)


CONCEPTS TO DISCUSS

1. Why are InvalidAmountException and friends checked in this lab, while NullPointerException is unchecked?

The domain ones model failures the caller can actually do something about - bad amount, no funds, wrong PIN, account not found. Extending Exception makes the compiler force a catch or a throws at every call site so the recovery path can't be skipped by accident. NPE is a programming defect not a business outcome, there's no sensible recovery, you fix the dereference. Making it checked would just mean boilerplate around every field access.

2. What does throws on Account.withdraw(...) force callers to do?

Catch it or declare it themselves, catch-or-declare. withdraw declares InvalidAmountException and InsufficientFundsException so ATMService can't call it and quietly ignore the failure, that won't compile. Pushes the decision up to whoever can actually recover instead of guessing at the bottom. Proved it in Ex 4 and 6, dropping the throws gives "unreported exception ... must be caught or declared to be thrown".

3. Why catch specific exceptions before a broad catch (Exception ex)?

Catch blocks are tested top down, first match wins, so a broad catch first makes everything under it unreachable - straight compiler error for checked types. The bigger reason is context: the specific catch is the only place you still know what went wrong. InsufficientFundsException carries requested + balance, InvalidPinException carries attemptsRemaining. Collapse to Exception and that's gone, all you can print is a generic failure.

4. What guarantee does finally give you that catch alone does not?

catch only runs when something matching was actually thrown. finally runs on both paths, success and handled failure, so cleanup lives in one place instead of being copied to the end of try and into every catch. Ex 2 showed it, cleanup printed twice off two transfer() calls, one each way. Normal guarantee not an absolute one, System.exit or a killed JVM skips it.

5. Why prefer try-with-resources over reader.close() in a finally block?

It closes at the end of the block automatically, reverse order, and can't be forgotten. Real win is suppressed exceptions - if the body throws and close() throws too, the finally version replaces the original with the close failure and the actual cause disappears. try-with-resources keeps the first one and hangs the close failure off it as suppressed. Ex 3 read transactions.txt with no finally block at all.

6. Why log stack traces to a file while showing short messages to the ATM user?

Two different audiences. Someone standing at the machine needs "Transaction Failed. Insufficient Account Balance." and nothing more, a stack trace tells them nothing and leaks internals - class names, file paths, how the thing is put together. The operator reading logs/application.log needs the opposite, LoggerUtil.logError writes timestamp + level + message + the exception type and frames. Also the obvious one, PINs and account details never go in a log or a screenshot.

7. Where should validation throw - deep in Account or only in Main? Why?

In Account, where the rule and the state both live. amount <= 0 and amount > balance are both checked before balance -= amount runs, so a rejected withdrawal leaves the balance untouched, same mutation-order point as Ex 5. Validate only in Main and every future caller - transfer, a batch job, a test - can walk straight past it and corrupt state. Main is the boundary that catches, logs and recovers, not the place that decides what counts as valid.

8. How will CRM later reuse "domain exception + boundary catch + log"?

Same shape, different nouns. Rule violations become checked exceptions carrying their own context (a DuplicateContactException holding the conflicting id, the way InsufficientFundsException holds requested + balance), service methods declare instead of swallowing, one boundary catches, logs the detail and shows the user something safe, then carries on. Not building CRM today, the layering judgment is what carries over.



REFLECTION QUESTIONS

1. What is the difference between checked and unchecked exceptions?

Checked extends Exception, compiler forces catch-or-declare. Unchecked extends RuntimeException, no obligation. All four custom ones here are checked so ATMService can't call Account.withdraw and ignore it. The NPE / arithmetic / bounds demos compile with no catch at all, builds clean and dies at runtime.

2. Why should custom exceptions be used?

A bare Exception hands the catch block a string and nothing else. A custom type gives a specific catch plus somewhere to hang context. InsufficientFundsException carries requestedAmount + availableBalance so the log writes "Requested 20000.0 Balance 11000.0" without recomputing it. InvalidPinException carries attemptsRemaining, which drives the session lock.

3. What is exception propagation?

A throw unwinds outward frame by frame until something catches it. Checked ones have to be declared by every frame on the way. Lab chain is Main to ATMService.withdraw to Account.withdraw to throw, caught at executeTransaction. Ex 6 printed all four frames, first at line is the throw site.

4. What is the purpose of finally?

Cleanup on both paths. catch only fires on failure, and anything at the end of try gets jumped past when something throws. printReturnMessage sits in finally, so the menu text prints after a good deposit and after Insufficient Balance. System.exit or a killed JVM still skips it.

5. Why is try-with-resources preferred?

Closes automatically, reverse order, can't be forgotten. With close in a finally, if the body throws and close throws too then the close failure replaces the original and the real cause is gone. try-with-resources keeps the first and attaches the close failure as suppressed. loadTransactionsFromFile and generateDailyErrorReport both use it.

6. When should throw be used?

In a body, when the method can't continue and can't fix it itself. Signals one object with the context already filled in. Account.withdraw throws InvalidAmountException for amount <= 0 and InsufficientFundsException for amount > balance, both before balance -= amount. Not for branching you could write as an if.

7. When should throws be used?

On the signature, when the method lets a checked exception travel because it can't recover. Declares only, throws nothing by itself. Account.deposit and withdraw declare theirs so ATMService has to handle them. Declare when catching adds nothing, catch when you can recover or add context.

8. Why is logging important in enterprise applications?

The failure happens on someone else's machine and the log is all you get. User sees a one-liner, log gets type, message, frames, timestamp. LoggerUtil.logError writes it to logs/application.log and the daily report counts it back out. PINs and balances never go in.

9. What happens if an exception is not handled?

Unwinds out of main, default handler dumps the trace to stderr, thread dies. Single-threaded means the process dies with it, exit 1. Ex 1 with the array catch removed: the earlier catch still worked, then the bounds one went uncaught and "Program continued." never printed. In the ATM one failed withdrawal would kill the session.

10. How does proper exception handling improve software reliability?

Failure stops being all-or-nothing. Every recoverable path prints a message, logs the detail, returns to the menu with the process alive. State stays consistent because validation runs before mutation. catch-or-declare makes the recovery path a compile-time obligation, not something you remember to write.

11. (Forward look) How would a future CRM map domain exceptions to API errors using the same boundary-catch + log pattern?

Same layers, different boundary. Domain throws typed exceptions carrying their own context, services declare instead of swallowing. A controller-level handler catches by type and maps to a status plus a safe body, not found 404, validation 400, unmapped 500, full trace to the log. Same user vs operator split as the ATM. Not building CRM today.



CHECKPOINT A

| # | Confirm | Your notes  |
| - | ------- |-------------|
| 1 | `java-bootcamp/examples/Lab7-ATMSystem/src/com/academy/atm/` exists | PASS        |
| 2 | Four custom exceptions + `Account` + `transactions.txt` + `logs/` present | PASS|
| 3 | Seed accounts: `1001`/`1234`/$11000 and `1002`/`5678`/$5000 | PASS |
| 4 | Edited via IntelliJ (or optional VS Code) on your laptop | PASS|


### Checkpoint B — Service + Main compile

_Mark each row **Pass** or **Fail** in your lab notes (GitHub markdown files are not interactive checklists)._

| # | Confirm | Your notes |
| - | ------- | ---------- |
| 1 | `ATMService`, `LoggerUtil`, `Transaction`, `Main` present | PASS|
| 2 | `javac -d out src/com/academy/atm/*.java` succeeds | PASS|
| 3 | `java -cp out com.academy.atm.Main` from **project root** shows menu 1–7 | PASS|
| 4 | Exit prints `Thank You` and terminates | PASS |

### Checkpoint C — Exception behavior

_Mark each row **Pass** or **Fail** in your lab notes (GitHub markdown files are not interactive checklists)._

| # | Confirm | Your notes |
| - | ------- | ---------- |
| 1 | Withdraw more than balance (e.g. `20000` on `1001`) → Insufficient Balance; menu continues | PASS|
| 2 | Invalid amount / bad PIN / missing account produce ERROR messages (not crashes) | PASS|
| 3 | Invalid numeric input shows the Part 3 messages and continues | PASS |
| 4 | `finally` prints return-to-menu text after operations | PASS |
| 5 | try-with-resources handles missing/unreadable `transactions.txt` with the IOException message | PASS |

### Checkpoint D — Logging + evidence

_Mark each row **Pass** or **Fail** in your lab notes (GitHub markdown files are not interactive checklists)._

| # | Confirm | Your notes |
| - | ------- | ---------- |
| 1 | `logs/application.log` contains ERROR (and ideally INFO) entries under the project root |PASS |
| 2 | Exception hierarchy notes filled; reflection drafted | PASS|
| 3 | Screenshots of success **and** failure paths saved (no secrets) |PASS |

MANUAL VERIFICATION

  1   menu 1-10 appears, invalid xyz -> invalid menu option -> menu returns      Pass
  2   login 1001 / 1234 -> Login Successful, seed balance 11000                  Pass
  3   withdraw 20000 -> Insufficient Balance / Transaction Cancelled, still up   Pass
  4   deposit -100 -> Amount must be greater than zero.                          Pass
  5   deposit abc -> Invalid numeric input. / Please enter a valid amount.       Pass
  6   deposit 1000 -> Deposit Successful, balance 12000                          Pass
  7   mini statement shows session rows + 4 historical lines from file           Pass
  8   login 9999 -> Account not found: 9999, still at menu                       Pass
  9   menu 8 unchecked demo prints three handled messages                        Pass
  10  logs/application.log has ERROR entries, exit 7 -> Thank You                Pass

Row 5 only works because readAmount consumes the whole line itself. The starter
had an extra scanner.nextLine() in the InputMismatchException catch, which eats
the next menu choice. Guide's Step 8 version drops it, kept that.


SUCCESS CRITERIA

0  Module 7 Exercises 1-8 Pass before Lab Step 1 (see PASS CRITERIA): Pass
1  work in examples/Lab7-ATMSystem/ with package com.academy.atm: Pass
2  four custom exceptions + login/deposit/withdraw, insufficient-funds path works: Pass
3  menu recovers after every failure, logs/application.log has INFO + ERROR: Pass
4  javac -d out and java -cp out com.academy.atm.Main from project root: Pass
5  can narrate throw site -> catch boundary -> log -> return to menu (exception-hierarchy.md): Pass
6  screenshots under notes/screenshots/lab-7/, seed PIN only, no real secrets: Pass


Evidence: screenshots in notes/screenshots/lab-7/. 1.png login success + already
logged in, 2.png deposit -100 rejected then deposit 1000 -> 12000, 3.png withdraw
20000 Insufficient Balance, 7.png exit Thank You. Log at
examples/Lab7-ATMSystem/logs/application.log.
