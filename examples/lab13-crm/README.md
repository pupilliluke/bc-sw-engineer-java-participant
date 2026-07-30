Northstar CRM SOAP contract (Lab 13)

Contract-first design pack for the Northstar customer service. Two contract
files, eight sample envelopes, two design docs. No server, no Java, no build.

  contracts\customer.xsd            types + request/response payloads
  contracts\CustomerService.wsdl    three operations, document/literal, placeholder address
  samples\*.xml                     six success envelopes + two faults
  docs\operation-matrix.md          scope table, faults per operation, what is out
  docs\soap-design-notes.md         decisions, retry semantics, Lab 24 handoff

THE PLACEHOLDER ADDRESS IS NOT LIVE

  <soap:address location="http://localhost:8080/ws"/>

Nothing listens on 8080 in this lab. The address is a structurally valid
placeholder so the WSDL is complete and tooling can load it, and that is all it
is. Lab 24 hosts the real endpoint with Spring-WS.

Checked during this lab, not assumed:

  port 8080: not listening - connection refused (expected)

Do not start Spring Boot or Tomcat to make the URL answer. That is Lab 24 and
starting it here is scope creep.

NAMESPACE AND OPERATIONS

  Namespace   http://northstar.com/crm/customer
  Style       document/literal
  Operations  CreateCustomer, UpdateCustomer, GetCustomer
  soapAction  http://northstar.com/crm/customer/<Operation>

Activation is UpdateCustomer carrying status=ACTIVE. There is no
ActivateCustomer operation; adding one buys clearer intent and costs every
partner a regeneration, so it stays out until activation has rules of its own.

FIXTURES

  CUS-1001   Amina Khan   ACTIVE     amina.khan@example.com
  CUS-1002   Ravi Singh   PROSPECT   ravi.singh@example.com
  CUS-9999   never created, the not-found fixture
  lab-request-001   correlation id, echoed in responses and faults

Same customers and statuses as Labs 10 to 12. All sample data is fictional.

VERIFY

Well-formedness, PowerShell, no xmllint needed:

  Get-ChildItem contracts,samples -Recurse -Include *.xsd,*.wsdl,*.xml | ForEach-Object {
    $null = [xml](Get-Content -Raw $_.FullName); "OK $($_.Name)"
  }

Result: 10/10 well-formed.

Schema validation of the six success payloads, which is stronger than
well-formedness. The SOAP envelope itself needs the SOAP wrapper schema to
validate, so the check extracts the Body payload and validates that against
customer.xsd:

  $schemas = New-Object System.Xml.Schema.XmlSchemaSet
  $schemas.Add("http://northstar.com/crm/customer", "contracts\customer.xsd")
  # then per sample: SelectSingleNode("/s:Envelope/s:Body/*") -> Validate()

Result: 6/6 payloads valid against the XSD. Fault envelopes are excluded on
purpose, their detail children are untyped and not declared in the schema.

Neither check needs a network. That is the point of a design-time contract.

PARTNER HANDOFF CHECKLIST

| # | Confirm | Result |
| - | ------- | ------ |
| 1 | Namespace URI published | Pass, http://northstar.com/crm/customer, in the WSDL, the XSD and docs |
| 2 | WSDL location placeholder documented | Pass, localhost:8080/ws, documented above as not live and commented in the WSDL |
| 3 | Three operations named and described | Pass, docs\operation-matrix.md with inputs, outputs and faults |
| 4 | Sample success envelopes for CUS-1001 / CUS-1002 | Pass, six envelopes, create and get and update, request and response |
| 5 | Fault examples for not-found and validation | Pass, fault-customerNotFound.xml and fault-validation.xml |
| 6 | Correlation ID convention | Pass, lab-request-001, optional element on every request, echoed in both faults |
| 7 | Explicit note: implementation arrives in Lab 24 | Pass, above and in docs\soap-design-notes.md section 10 |
| 8 | Optional: screenshot of XSD/WSDL outline | notes\screenshots\lab-13 |

WHAT A PARTNER CANNOT TELL FROM THIS PACK

  Blank customerId passes the schema. xs:string accepts an empty element, so
  <customerId></customerId> validates and is still not a usable request. The
  rejection is a service rule and arrives with Lab 24.

  Invalid status transitions are documented as a business fault and enforced
  nowhere. CLOSED to ACTIVE is four legal characters in a legal element.

  CUS-#### is a documented ID shape, not an xs:pattern. Enforcing it later
  would be a breaking change, so it is written down instead.

Details and the reasoning are in docs\soap-design-notes.md sections 6 and 8.

FAILURE EXPERIMENTS RUN

| # | Experiment | Observed | Conclusion |
| - | ---------- | -------- | ---------- |
| 1 | schemaLocation renamed to customer-RENAMED.xsd | import unresolved, no such file beside the WSDL; restored, resolves and compiles again | keep the XSD beside the WSDL, relative schemaLocation is the coupling |
| 2 | Empty customerId in a GetCustomer payload | schema verdict VALID | well-formed and schema-valid is not the same as a usable request |
| 5 | TCP connect to 127.0.0.1:8080 | connection refused | expected, design-only lab, not a failure |

Experiment 3, retry safety of Create vs Get, is reasoning rather than a run and
lives in docs\soap-design-notes.md section 5. Create is the unsafe one, a retry
after a lost response creates a second customer.

NOW VS LATER

  NOW, Lab 13     contract artifacts, sample envelopes, design docs. Static files.
  SOON, Lab 24    Spring-WS @Endpoint methods implement this contract against CustomerService.
  LATER           React, Kafka, PostgreSQL. Not modelled here.

Keep this project. Lab 24 builds on it rather than replacing it.

SECURITY

No WS-Security and no transport security in this lab, because nothing is hosted.
Untrusted input is every body element; schema validation is the first gate and
service rules are the second. Logging carries the correlation id only, never
email, phone or full name. Sample data is fictional. The deferral table is in
docs\soap-design-notes.md section 9.

CLEANUP

  git status

Nothing to stop, no containers, no ports, no target directory. The pack is the
deliverable.
