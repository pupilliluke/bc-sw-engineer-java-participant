Lab 7 ATM System - exception hierarchy

All four custom ones extend Exception directly, so they're checked and the
compiler forces catch-or-declare at every call site. None of them extend
RuntimeException on purpose - these are business outcomes the caller can
recover from, not programming defects.

  Throwable
    Exception                              checked
      InvalidAmountException
      InsufficientFundsException           + requestedAmount, availableBalance
      InvalidPinException                  + attemptsRemaining
      AccountNotFoundException
      IOException                          JDK, file read / log write
      RuntimeException                     unchecked
        NullPointerException
        ArithmeticException
        ArrayIndexOutOfBoundsException
        InputMismatchException
        NumberFormatException


THE FOUR CUSTOM ONES

  exception                     thrown                          caught
  InvalidAmountException        Account.deposit, .withdraw      ATMService.executeTransaction
  InsufficientFundsException    Account.withdraw                ATMService.executeTransaction
  InvalidPinException           ATMService.login, .requireLogin ATMService.login / executeTransaction
  AccountNotFoundException      ATMService.findAccount          ATMService.login / executeTransaction

Two of them carry structured context, not just a message. InsufficientFundsException
holds requestedAmount + availableBalance so the log line can say "Requested 20000.0
Balance 11000.0" without the catch block recomputing anything. InvalidPinException
holds attemptsRemaining, which is what drives both the "Attempts remaining : 2"
message and the session lock. The other two are message-only, nothing extra to carry.


JDK ONES THAT SHOW UP

  IOException              loadTransactionsFromFile, LoggerUtil.writeLog, generateDailyErrorReport
                           checked, so try-with-resources + catch is forced
  InputMismatchException   readAmount rethrows it when Double.parseDouble fails
                           unchecked, but caught first in executeTransaction for the Part 3 message
  NumberFormatException    Main.main on the menu choice, and inside readAmount
  NPE / Arithmetic / AIOOBE  demonstrateUncheckedExceptions only, deliberate


WHERE THE BOUNDARY IS

Account throws, never catches - it owns the rules and the balance, so validation
runs before balance -= amount and a rejected withdrawal leaves state untouched.

ATMService.executeTransaction is the single catch boundary for every transaction.
Order is specific to general: InputMismatchException, InvalidAmountException,
InsufficientFundsException, then multi-catch InvalidPinException | AccountNotFoundException,
then catch Exception last. Broad first wouldn't compile - the specific blocks
under it are unreachable. finally calls printReturnMessage so the menu text prints
on both paths.

login catches its own AccountNotFoundException | InvalidPinException instead of
going through executeTransaction, because it isn't a transaction and it needs the
attempts-remaining logic in the handler.

transferFunds is the one place with a nested catch. It restores both balances from
the values captured before the withdraw, then rethrows so the outer multi-catch
still logs and messages normally. Catching to recover and then rethrowing, not
catch-and-swallow.

Main catches NumberFormatException on the menu choice so a typo loops instead of
killing the app. Everything else is already handled below it.
