Lab 13 SOAP API design (key ideas, reflection questions, checkpoints, manual
verification, failure experiments)

built under examples\lab13-crm. two contract files, eight sample envelopes, two
design docs, a README with the handoff checklist. no java, no build, no server.


KEY IDEAS

1. Main data flow

partner builds an envelope against the published wsdl, sends it to the address
in the service element, a future spring-ws endpoint unmarshals it and calls
CustomerService. today the middle two steps do not exist, the contract and the
samples do. lab 24 fills the gap without changing the contract.

2. Trust boundary, schema vs service rules

the schema is the first gate and it runs before any code. it catches a status
outside the four enumerations, a missing required element, a malformed
timestamp. everything else is a service rule, blank ids, duplicate ids, status
transitions. experiment 2 is the proof, an empty customerId passes the schema.

3. Success and failure contract for an unknown id

success is getCustomerResponse wrapping a CustomerType. an unknown id is a
soapenv:Client fault with CUSTOMER_NOT_FOUND in the detail and the id and the
correlation id alongside it. CUS-9999 carries that example because it is never
created anywhere, so it can never resolve by accident.

4. Stable identity vs display fields

customerId is the identity, it is what equals compares in lab 12 and what the
partner stores. fullName, email, phone and status are facts about the customer
that change. that split is why the fault detail carries customerId and never
carries a name or an email.

5. Retry and idempotency

GetCustomer is safe to retry, it reads. UpdateCustomer is safe for the same
target, twice ACTIVE leaves ACTIVE. CreateCustomer is not safe, a retry after a
lost response makes a second customer, and the contract has no idempotency key
to prevent it. that is the one to tell partners about.

6. Static wsdl files vs generating at runtime

a static wsdl is a file the partner can read, diff and version. a runtime
generated one moves whenever the code moves, which is the code-first failure
mode from exercise 4. lab 24 should serve this file, not derive a new one.


REFLECTION QUESTIONS

1. Which design decision most affected partner usability?

Putting correlationId in the request body as an optional element. It validates
against the same schema as everything else and needs no WS-Addressing, so a
partner gets support traceability for free. The cost is that it is omittable,
which is why both faults carry their own copy.

2. What evidence proves the contract is implementable in Lab 24?

Every operation maps onto a method lab 12 already has. GetCustomer to
getCustomer, CreateCustomer to createCustomer, UpdateCustomer with status to
updateStatus, and both fault samples restate exceptions that class already
throws. The gap is UpdateCustomer's non-status fields, which have no method yet.

3. Which failure was hardest to diagnose, namespace or element name?

Neither, the schemaLocation break in experiment 1. A wrong namespace shows up
immediately as a mismatch, a renamed import fails quietly at resolve time and
the wsdl still looks complete on screen. Nothing in the file itself says what is
wrong.


IMPLEMENTATION CHECKPOINTS

| Checkpoint | Confirm | Result |
| --- | --- | --- |
| A, layout + xsd core | contracts, samples, docs exist; CustomerType and CustomerStatus written | Pass |
| B, full contract | six global elements, three messages pairs, portType, document/literal binding, placeholder address | Pass |
| C, samples + faults | eight envelopes, three operations, two faults | Pass |
| D, handoff + experiments | README checklist complete, three experiments run and recorded | Pass |


MANUAL VERIFICATION

| # | Check | Result |
| --- | --- | --- |
| 1 | create, get, update represented in samples | Pass, six success envelopes |
| 2 | fault samples cover not-found and validation | Pass, both present |
| 3 | broken schemaLocation experiment recorded and restored | Pass, experiment 1, restored and re-verified |
| 4 | identifiers and correlation visible in samples | Pass, CUS-1001, CUS-1002, lab-request-001 |
| 5 | no server required, contracts are static files | Pass, nothing started |
| 6 | no secrets in git | Pass, fictional example.com addresses and 555 numbers only |
| 7 | second student can open the wsdl without my IDE settings | Pass, relative schemaLocation, no IDE metadata committed |
| 8 | well-formedness passes | Pass, 10/10 via PowerShell [xml] |
| 9 | README states lab 24 implements runtime soap | Pass |
| 10 | namespace matches lab 24 continuity | Pass, http://northstar.com/crm/customer |

extra check beyond the list, the six success payloads validate against
customer.xsd, not just well-formed. the envelope needs the soap wrapper schema
so the check pulls Body's child and validates that. 6/6 valid.


FAILURE EXPERIMENTS


1. schemaLocation renamed

    before:   schemaLocation='customer.xsd' -> RESOLVED, 1 schema compiled
    broken:   schemaLocation='customer-RENAMED.xsd' -> UNRESOLVED, no such file beside the WSDL
    restored: schemaLocation='customer.xsd' -> RESOLVED, 1 schema compiled

the wsdl stayed well-formed the whole time, which is the lesson. a partner
loading it sees a complete looking document and only finds out at resolve time.
keep the xsd beside the wsdl.

2. empty customerId

    schema verdict: VALID - empty customerId passes xs:string

expected it to fail and it does not. xs:string accepts an empty element, so
<customerId></customerId> is well-formed and schema-valid and still useless.
rejecting it is lab 12's requireNonBlank, a service rule, and the contract
cannot promise it.

3. placeholder endpoint

    port 8080: not listening - connection refused (expected)

design-only lab, so refused is the correct result and not a failure. recorded
because the README claims the address is not live and the claim should be
checked rather than asserted.

experiment 3 from the guide's table, create vs get retry safety, is reasoning
rather than a run, it lives in docs\soap-design-notes.md section 5.


EVIDENCE

notes\screenshots\lab-13 for the IDE outline screenshot. the two verification
runs above are pasted from the actual PowerShell output, not retyped.
