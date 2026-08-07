# Lab 25 — Layer Boundary Quiz

| Responsibility | Layer (Controller / Service / Repository) |
| --- |-------------------------------------------|
| Map JSON ↔ HTTP status | Controller                                |
| Reject duplicate CUS-1001 | Service                                   |
| Store Customer by id | Repository                                |
| PROSPECT → ACTIVE rule | Service                                   |
| May import CustomerRepository? | Service                                   |


## Debug / design challenge

Rewrite a controller that calls map.put directly into proper layers.

Response = map.put(CUS-1001, ACTIVE) ???
 
## Predict the Output / Behavior

If ResponseEntity appears inside CustomerService, which layer leaked?

Controller


Self-check before marking Pass:

- [ x ] File exists at `notes/layers.md`
- [ x ] Five rows filled
- [ x ] Controller cannot import repository