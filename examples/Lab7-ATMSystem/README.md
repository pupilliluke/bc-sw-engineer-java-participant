Lab 7 - ATM Banking System (exception handling and error management)


OVERVIEW

Console ATM that stays alive under bad input and broken business rules. Two seed
accounts, 1001/1234 at 11000 and 1002/5678 at 5000. Menu covers login, deposit,
withdraw, balance, transfer, mini statement, exit, an unchecked exception demo,
and two bonus reports.

The point of the lab isn't the ATM, it's where failure gets handled. Account
throws and never catches. ATMService catches at one boundary. Main only handles
a bad menu choice. Nothing that a user can type kills the process.


EXCEPTION HIERARCHY

Full write-up in notes/Week 1/Module 7/exception-hierarchy.md. Short version:

  Exception (checked)
    InvalidAmountException
    InsufficientFundsException      + requestedAmount, availableBalance
    InvalidPinException             + attemptsRemaining
    AccountNotFoundException
    IOException                     JDK, file read and log write
    RuntimeException (unchecked)
      InputMismatchException, NumberFormatException, NPE, Arithmetic, AIOOBE

All four custom ones extend Exception directly, not RuntimeException. They model
business outcomes a caller can recover from, so catch-or-declare should be a
compile-time obligation. The unchecked ones in demonstrateUncheckedExceptions
are programming defects and are only there to prove the menu survives them.


CUSTOM EXCEPTIONS

  exception                     thrown                       caught
  InvalidAmountException        Account.deposit, .withdraw   executeTransaction
  InsufficientFundsException    Account.withdraw             executeTransaction
  InvalidPinException           login, requireLogin          login / executeTransaction
  AccountNotFoundException      findAccount                  login / executeTransaction

Two carry structured context instead of just a message. InsufficientFundsException
holds requestedAmount + availableBalance so the log writes "Requested 20000.0
Balance 11000.0" without the handler recomputing it. InvalidPinException holds
attemptsRemaining, which drives the "Attempts remaining : 2" message and the
session lock after three failures.


LOGGING STRATEGY

LoggerUtil appends to logs/application.log, created on first write. Three entry
points: logInfo, logError (message + exception type + frames), logTransaction
(message + elapsed ms).

Split by audience. The user at the machine gets one safe line, "Insufficient
Balance". The log gets timestamp, level, message, exception type and stack
frames. PINs and balances never go in a log line or a screenshot.

Every catch in executeTransaction logs before it recovers, so a recovered failure
still leaves a trace. Menu 9 reads the log back and counts ERROR entries per day
with a preview of the most recent five.


COMPILE AND RUN

Working directory has to be the project root or transactions.txt and logs/ won't
resolve.

  cd $env:USERPROFILE\java-bootcamp\examples\Lab7-ATMSystem
  javac -d out src\com\academy\atm\*.java
  java -cp out com.academy.atm.Main
  Get-Content logs\application.log -Tail 40



SAMPLE OUTPUT

  Choice : 1
  Enter Account Number : 1001
  Enter PIN : 1234
  Login Successful
  Transaction Completed.
  Returning to Main Menu.

  Choice : 3
  Amount : 20000
  ERROR
  Insufficient Balance
  Transaction Cancelled
  Transaction Completed.
  Returning to Main Menu.

  Choice : 2
  Amount : 1000
  Deposit Successful
  Current Balance : 12000
  Transaction Completed.
  Returning to Main Menu.

  Choice : 7
  Thank You

"Transaction Completed." printing after a cancelled transaction looks wrong but
it's the guide's specified finally output. finally runs on both paths, that's
the whole reason the line is there.


BONUSES

  1  transfer rollback           implemented, restores both balances and rethrows
  2  daily error report          menu 9, ERROR count per date + last 5 preview
  3  execution time logging      logTransaction on every successful transaction
  4  login retry lock            3 attempts, then login refuses before prompting
  5  transaction summary         menu 10, total / successful / failed + by type


LESSONS LEARNED

Message bugs don't announce themselves. The Ex 4 typo threw the right exception
with the wrong text and still compiled, ran and passed a casual glance. Same with
the format string in Ex 5, super(String.format(...)) vs super("...%.2f") is a
one-word difference that prints literal %.2f to the user.

Derived values belong where the data is. Computing requested minus balance in the
catch block means every caller can get the operands backwards and silently print
a negative shortfall. Putting it on the exception makes it right once.

Catch order is a compiler rule, not a style preference. Broad first makes the
specific blocks below unreachable and won't build for checked types.

Recording sites need consistent keys. Successes were logged as "DEPOSIT" and
failures as "Deposit", which looked fine everywhere until the summary report
grouped by type and showed two buckets for one operation.

A message about a lock isn't a lock. login printed "Maximum PIN attempts reached"
while the counter went negative and kept accepting attempts. The guard had to be
added at the top of the method, before the prompt.
