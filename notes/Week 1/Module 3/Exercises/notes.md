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