# Lab 37 — CRM ER diagram

```mermaid
erDiagram
    CUSTOMER ||--o{ ACCOUNT : owns
    CUSTOMER ||--o{ ADDRESS : "has postal"
    CUSTOMER ||--o{ CUSTOMER_STATUS_HISTORY : "logs transitions"

    CUSTOMER {
        bigint customer_id PK "identity surrogate"
        varchar public_id UK "CUS-1001, immutable"
        varchar full_name
        varchar email_normalized UK "lowercased"
        varchar phone
        varchar status "PROSPECT ACTIVE SUSPENDED CLOSED"
        integer version_no
        timestamptz created_at
        timestamptz updated_at
    }
    ACCOUNT {
        bigint account_id PK
        varchar account_number UK "ACCT-1001-01"
        bigint customer_id FK
        varchar account_type "CHECKING SAVINGS CREDIT"
        varchar status "OPEN CLOSED FROZEN"
        numeric balance "NUMERIC(19,2)"
        char currency "CAD"
        timestamptz opened_at
    }
    ADDRESS {
        bigint address_id PK
        bigint customer_id FK
        varchar address_type "HOME WORK BILLING OTHER"
        varchar line1
        varchar line2
        varchar city
        varchar region
        varchar postal_code
        char country_code
    }
    CUSTOMER_STATUS_HISTORY {
        bigint history_id PK
        bigint customer_id FK
        varchar old_status
        varchar new_status
        varchar changed_by
        varchar reason
        varchar correlation_id "lab-request-001"
        timestamptz changed_at
    }
```

## Cardinalities

```text
Customer 1 ---- 0..* Account
Customer 1 ---- 0..* Address
Customer 1 ---- 0..* StatusHistory
```

Zero is the part that matters. Ravi Singh (`CUS-1002`, PROSPECT) has no
account and no address, and the schema has to accept him — a PROSPECT is a
customer the business has not sold anything to yet. Drawing the account
side as mandatory would make onboarding impossible.

## Delete rules

| Child | ON DELETE | Why |
| --- | --- | --- |
| account | RESTRICT | a financial record; deleting the customer must fail loudly, not take balances with it |
| address | CASCADE | only describes its customer, worthless once the customer is gone |
| customer_status_history | CASCADE | same lifetime as the customer, kept while the customer exists |

## Identifiers

| Identifier | Role |
| --- | --- |
| customer_id | identity surrogate PK, joins and FKs only, never shown |
| public_id | immutable business id, `CUS-1001`, what the API and UI carry |
| email_normalized | lowercased unique lookup, changes over a customer's life |
| account_number | unique business account identifier |

Email is never the primary key: people change addresses, and a PK must be
stable. `public_id` is separate from `customer_id` so the surrogate can be
an integer for join performance while the outside world sees a string that
never moves.
