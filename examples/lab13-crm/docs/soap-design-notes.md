# SOAP design notes — Lab 13

Decisions behind `contracts/customer.xsd` and `contracts/CustomerService.wsdl`,
and what Lab 24 inherits. Nothing here is hosted; see the README for what the
placeholder address does and does not mean.

## 1. Contract-first vs code-first for partners

Contract-first. The consumer is an external billing partner, and the contract is
the thing they hold and generate against, so it has to be editable on purpose
rather than as a side effect of a refactor.

Lab 12 is the argument. Renaming `doStuff` to `createCustomer` / `getCustomer` /
`updateStatus` was a safe refactor because nothing outside the project called
it. The same rename against a published WSDL is three operation renames and a
breaking release. Code-first makes the contract move whenever the class moves.

The second risk is leakage. Lab 12's service still carries the Lab 8 DTO stubs
(`create`, `getById`) that throw `UnsupportedOperationException`, and equality
built on `customerId`. A generator cannot tell which members are the domain and
which are scaffolding, so a code-first contract publishes both.

Honest caveat: the Java existed first. This contract is written after the code,
which is not contract-first in the strict sense. What keeps it useful is that
nothing is published yet and the class does not get the final say — see §6.

## 2. Document/literal

`style="document"` with `use="literal"` on all three operations, and one global
request element and one global response element each. Every message part points
at an element in `customer.xsd` rather than declaring a parallel type.

Reasons: it is the widely interoperable choice for a partner using unknown
tooling, the body is a single wrapper element that validates against a published
schema on its own, and `soapAction` stays explicit per operation
(`http://northstar.com/crm/customer/<Operation>`), so routing does not depend on
sniffing the body.

## 3. Correlation placement

`correlationId` is an optional element on each request body, not a SOAP header.

- There is no WS-Addressing in this lab and no header schema to hang it on.
- The value is business-support context, not a processing directive, so nothing
  needs `mustUnderstand`.
- Keeping it in the body means it validates against the same schema as the rest
  of the payload.

Cost of that choice: because it is `minOccurs="0"`, a partner can omit it, and a
fault raised on a request without one has nothing to echo. That is why both
fault samples carry `lab-request-001` in the `faultstring` *and* in `detail` —
a fault has to carry its own copy, the request is gone by then.

Convention is `lab-request-001` style: one id per partner request, echoed in
every response and fault for that request. Same value Lab 12 concatenates into
its failure messages.

## 4. Fault shapes: not-found vs validation

Both are `soapenv:Client` — the sender caused them and replaying the identical
envelope produces the identical answer. `Server` would invite a retry that fails
forever.

| Sample | faultcode | detail errorCode | Raised when |
| ------ | --------- | ---------------- | ----------- |
| `fault-customerNotFound.xml` | `soapenv:Client` | `CUSTOMER_NOT_FOUND` | well-formed ID that does not exist (CUS-9999) |
| `fault-validation.xml` | `soapenv:Client` | `VALIDATION_FAILED` | required field blank (`fullName`) |

The split matters: `CUS-9999` matches the documented ID shape, so it is a lookup
failure, not a validation failure. A malformed ID would be the other one.

`faultstring` mirrors Lab 12's own message text (`Customer not found: <id>`,
`fullName must be provided`) so the fault a partner reads and the exception the
service throws say the same thing, and Lab 24 has one less translation to write.

Known gap: the `detail` children (`errorCode`, `customerId`, `field`,
`correlationId`) are not declared in `customer.xsd`, so they are untyped. Typed
fault detail elements plus `wsdl:fault` declarations on the portType are the
first thing to add if partners need to branch on faults from generated stubs.
Deferred deliberately — it changes generated code, so it wants a version bump
rather than a quiet edit.

Never in a fault: `email`, `phone`, `fullName` values. A fault is the message
most likely to be pasted into a ticket or a screenshot.

## 5. Retry and idempotency

| Operation | Safe to retry? | Why |
| --------- | -------------- | --- |
| GetCustomer | yes | pure read, no state change |
| UpdateCustomer | yes, for the same target | setting `status=ACTIVE` twice leaves it ACTIVE; last write wins on other fields |
| CreateCustomer | no | a retry after a lost response creates a second customer |

`CreateCustomer` is the one to warn partners about. The contract has no
idempotency key today, so a timeout leaves the partner unable to tell "not
created" from "created, response lost". Options for Lab 24: let the partner
supply the ID and reject duplicates (Lab 12 already throws on a duplicate ID),
or add an optional idempotency key as a new optional element, which is a
backward-compatible addition.

## 6. What the schema cannot enforce

The trust boundary. Schema validation runs before any service code; business
rules run after.

| Rule | Enforced by schema? | Where it actually lives |
| ---- | ------------------- | ----------------------- |
| status is one of four values | yes, `CustomerStatus` enumeration | also the Java enum |
| `customerId` present | yes | — |
| `customerId` non-blank | **no** — empty string is valid `xs:string` (Experiment 2) | service rule, Lab 12's `requireNonBlank` |
| ID matches `CUS-####` | no — documented shape only, no `xs:pattern` | not enforced anywhere yet |
| ID is unique | no | Lab 12's `requireUniqueId` |
| CLOSED → ACTIVE is legal | no — all values are legal individually | nowhere yet; Lab 12's `updateStatus` has no transition check |
| email present | yes, required in `CustomerType` | **not** enforced by Lab 12's `createCustomer` |

Two of these are worth flagging to Lab 24:

- **Invalid status transition** is documented as a business fault and enforced by
  nothing. A partner cannot tell from the contract.
- **email** is required in the response type but optional in Lab 12's validation,
  so the service can currently build a customer that will not marshal. Either
  `createCustomer` starts requiring it or the element becomes `minOccurs="0"`.
  Decided in favour of the contract: the partner-facing type keeps email
  required, and Lab 24 tightens the service to match.

## 7. Java ↔ XSD mapping

Hand-written map, no JAXB generation in this lab.

| Java (`lab12-crm` `Customer`) | XSD |
| ----------------------------- | --- |
| `String customerId` | `xs:string`, required |
| `String fullName` | `xs:string`, required |
| `String email` | `xs:string`, required (see §6) |
| `String phone` | `xs:string`, `minOccurs="0"` |
| `CustomerStatus status` | `tns:CustomerStatus` enumeration |
| `LocalDateTime createdAt` | `xs:dateTime` |

`createdAt` is the mismatch. `LocalDateTime` carries no zone or offset;
`xs:dateTime` allows an offset and the samples use `Z`. As written, a value
produced by `LocalDateTime.now()` would serialise without the `Z` and mean
"17:00 somewhere". Lab 24 should convert at the boundary — `OffsetDateTime` or
`Instant` — or state UTC and convert once. Samples pin fixed timestamps rather
than printing what Lab 12 generates, which changes every run.

`absent` vs `nil`: the contract uses `minOccurs="0"` for optional fields and
never uses `nillable="true"`. Java has one null; the wire should have one way to
say missing.

## 8. Backward compatibility

Namespace is `http://northstar.com/crm/customer`, unversioned in the URI today.

- Additive changes (new optional element, new operation) stay in place.
- Breaking changes (renaming or removing an element, making an optional element
  required, adding a `wsdl:fault`) get a new namespace, `.../customer/v2`, with
  v1 kept live until the partner migrates.

Adding `xs:pattern` for `CUS-####` later would be breaking, which is why it is
documented rather than enforced now: widening a constraint is safe, narrowing is
not.

## 9. Security — deferred, with the boundary written down

No WS-Security in Lab 13, and no transport security to configure because nothing
is hosted.

| Concern | Lab 13 | Lab 24 and later |
| ------- | ------ | ---------------- |
| Untrusted input | every body element | schema validation at the boundary, then service rules |
| Transport | none — placeholder address is plain `http://localhost` | HTTPS/TLS |
| Authentication | none | WS-Security `UsernameToken` or transport auth |
| Parser hardening | n/a, no parser running | disable external entities (XXE), cap entity expansion, payload size, nesting depth |
| Logging | correlation ID only | same; never log email, phone, or full name |

Sample data is fictional on purpose: `example.com` addresses, `+1-555-01xx`
phone numbers.

## 10. Lab 24 forward link

Lab 24 implements this contract with Spring-WS `@Endpoint` methods against
`CustomerService`. It should not need to change the contract to do it.

Cross-walk:

| Contract operation | Lab 12 method |
| ------------------ | ------------- |
| CreateCustomer | `createCustomer(customerId, fullName, email, phone, status)` |
| GetCustomer | `getCustomer(customerId)` |
| UpdateCustomer | `updateStatus(customerId, newStatus)` for the status case; other mutable fields have no Lab 12 method yet |

`UpdateCustomer` is the loosest fit. Lab 12 only exposes `updateStatus`, so
Lab 24 either widens the service or narrows the operation. Activating Ravi —
`updateCustomerRequest` with `customerId=CUS-1002`, `status=ACTIVE` — maps
directly onto `updateStatus("CUS-1002", CustomerStatus.ACTIVE)`, which is the
line Lab 12's `Main` already runs.

Exceptions map to the fault samples: `IllegalArgumentException("Customer not
found: ...")` → `fault-customerNotFound.xml`, `IllegalArgumentException("...
must be provided")` → `fault-validation.xml`, duplicate ID
(`IllegalStateException`) → a third business fault this contract does not model
yet.
