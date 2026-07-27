

## Layer map

| Layer/package | Owns | Does not own |
| ------------- | ---- | ------------ |
| `controller` | Transport boundary, request/response mapping | Business rules, storage |
| `service` | Validation, orchestration, business policy | HTTP details, SQL |
| `repository` | Save/find abstraction | UI formatting, business workflow |
| `entity` | Domain state/identity | HTTP request shape |
| `dto` | Boundary input/output shape | Persistence behavior |
| `config` | Object/application configuration | Customer operations |
| `exception` | Meaningful failure types | Catch-all utility logic |
### Step 1 — Assign the tasks

| Task | Layer      |
| ---- |------------|
| Accept future create-customer input | Controller |
| Reject blank customer name | service    |
| Find customer by ID | repository |
| Represent customer ID/name/status | entity     |
| Represent create request fields | dto        |
| Define customer-not-found failure | exception  |
| Wire application objects later | config     |

Controller maps request
→ Service validates/orchestrates
→ Repository saves/finds
→ Service returns result
→ Controller maps response



## Pass criteria

| # | Confirm | Notes       |
| - | ------- |-------------|
| 1 | Seven tasks assigned correctly | PASS        |
| 2 | God-controller flow repaired | PASS |
| 3 | You explain at least two benefits of boundaries | PASS |
