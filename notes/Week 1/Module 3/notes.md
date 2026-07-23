# Banking domain notes

| Entity | Identity | Important attributes | Main responsibility |
| ------ | -------- | -------------------- | ------------------- |
| Customer | customerId | name, email, phone | Maintain customer profile |
| Account | accountNumber | owner, balance, accountType | Protect balance and perform deposits/withdrawals |
| Transaction | transactionId | account, type, amount, timestamp | Record one account operation |
## Relationships

- One Customer can own zero or more Accounts.
- One Account belongs to exactly one Customer.
- One Account can have many Transactions.
- One Transaction belongs to exactly one Account.

## Rules

- An account balance cannot be changed directly from outside Account.
- A deposit amount must be positive.
- A withdrawal cannot exceed the allowed balance.
- A transaction cannot be changed outside of customer

Why should Account, rather than Main, decide whether a withdrawal is valid?

Answer: Encapsulation is the concept that protects private information, such as a customer's Account, from being accessed by an unverified source(main). 
An customer's specific instance of account should privately hold the details necessary to decide whether a withdrawl is valid or not. The customer's balance should not be visible to the world .

## SRP spot-check

The original method could change because the formula changes or because
the output format changes. These are separate responsibilities.

Main should manage menu input, BankService should coordinate banking operations, and domain classes should protect their own state.



O — Open/Closed Principle
Idea: Open for extension, closed for modification — add new behavior by adding a class, not by editing existing ones.

Why: When CurrentAccount was added in Exercise 3, SavingsAccount and Account did not need edits for the new type to work.

Write one sentence confirming that adding FrozenAccount required no changes inside SavingsAccount or CurrentAccount.

RESPONSE ==> Correct, Frozen account did not need any modifications inside Savings/CurrentAccount.java

L — Liskov Substitution Principle
Idea: A subclass must be usable anywhere its parent type is expected, without breaking the caller's expectations.

Why: The loop expects every Account to return a boolean and leave balance unchanged on failure. FrozenAccount honors that contract instead of throwing an unexpected exception.

RESPONSE ==> Correct, the account balance still read just like the other methods, although unchanged.

I — Interface Segregation Principle
Idea: Prefer several small, focused interfaces over one large interface that forces classes to implement methods they do not need.

Why: Exercise 5's Printable has one method, printDetails().

Write one sentence: what would go wrong if Printable also required sendEmailReceipt(), and a SavingsAccount had no email system to call?

RESPONSE==> sendEmailReceipt() would only work for subclasses with EMAIL, and would not be able to be extended for other subclasses underneath printable's umbrella.

D — Dependency Inversion Principle
Idea: Depend on an abstraction, not a concrete class, so the concrete implementation can change later.

Why: Declaring Account account = new FrozenAccount(100.00); keeps the caller focused on the shared contract, not one subclass.

Write one sentence explaining why the base type makes later swaps easier.

RESPONSE==> It allows for more flexibility/interchangability. It allows the user to change their mind without changing the code in the target's parent class. 