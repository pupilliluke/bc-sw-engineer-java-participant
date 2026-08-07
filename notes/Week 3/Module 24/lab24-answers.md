Lab 24 SOAP web service endpoints (reflection questions, checkpoints)

built under examples\lab24-crm, copied from the lab 24 starter rather than
carried forward from lab23-crm. timed path: contract-first customer.xsd with
getCustomer only, WebServiceConfig wired for /ws/* and /ws/customers.wsdl, DOM
Element mapper, one @PayloadRoot, REST kept. 2 tests green on three consecutive
mvn -B test runs. app captured on port 8080 with both protocols.


REFLECTION QUESTIONS

1. Which design decision most affected correctness (contract-first)?

putting the xsd in front. the wsdl operation name, the port type and the field
names all fall out of customer.xsd, so there was nothing to keep in sync by
hand. it also settles the naming argument: the partner sees customerId because
the xsd says customerId, while the domain keeps id, and CustomerSoapMapper is
the only place that knows both.

2. What evidence proves SOAP and REST share rules?

CUS-2401 in 02-wsdl-soap-rest-manual.txt. it was created over REST and read back
over SOAP, which reading a shared seed would not have proved. two stores would
have faulted not-found on the SOAP read.

3. Which failure was hardest to diagnose (payload root vs WSS)?

neither. the payload root matched first try because the starter xsd and
get-customer.xml already agreed, and WSS is not wired. experiment 3 is the one
that would be hard from the client side. malformed xml comes back as HTTP 400
with an empty body, so there is nothing in the response to read. the SAAJ0511
line in the server log is the only place the cause shows up.


CHECKPOINTS

| Checkpoint | Confirm | Result |
| --- | --- | --- |
| A1 | lab24-crm under examples/ | Pass, copied from starter/ |
| A2 | Spring-WS + wsdl4j present, JAXB/XJC not required timed | Pass, spring-ws-core 4.0.11, wsdl4j 1.6.3, no XJC plugin |
| A3 | Timed customer.xsd has getCustomer request/response only | Pass, starter xsd unchanged |
| B1 | Live /ws/customers.wsdl with port type CustomersPort + getCustomer | Pass, portType CustomersPort, operation GetCustomer |
| B2 | CustomerEndpoint getCustomer delegates to CustomerService | Pass, three lines, no rules in the endpoint |
| B3 | DOM CustomerSoapMapper keeps XML out of service/REST layers | Pass, service and controller import no org.w3c.dom |
| C1 | Fault resolver / CLIENT not-found, full path | N/A timed, CUS-9999 returns faultcode Server, recorded in experiment 2 |
| C2 | Missing UsernameToken rejected, full path only | N/A timed, spring-ws-security absent, no interceptor registered |
| C3 | Timed: unsecured get CUS-1001 works | Pass, 200 with Amina Khan ACTIVE and no Security header |
| D1 | Two consecutive mvn test, Tests run: 2 | Pass, three runs at 2 |
| D2 | REST and SOAP share one service proof | Pass, CUS-2401 written over REST, read over SOAP |
| D3 | No secrets / target/ committed, UsernameToken marked lab-only | Pass, target/ ignored, example.com emails, no wsse block anywhere |

SECURITY AND PRODUCTION REVIEW

1. which SOAP fields are untrusted and where validated?

the whole body, and customerId in particular. the mapper checks the element is
present and nothing else, so the service decides what is found. same position as
REST, where the controller does not validate ids either.

2. is UsernameToken enough without HTTPS?

no. PasswordText puts the password on the wire in cleartext, so anyone on the
path reads it and can replay it. TLS underneath is the minimum before the token
means anything.

3. is plaintext PasswordText acceptable outside the lab? what replaces it?

no. PasswordDigest with a nonce and a timestamp, over TLS, with rotated secrets,
or a certificate if the partner supports it. crm-partner / lab24-shared-secret
are lab strings and are not in this build at all.

WINDOWS HOW-TO PASS CRITERIA

| # | Confirm | Result |
| - | --- | --- |
| 1 | workspace open in IntelliJ with SDK 21 | Pass, temurin-21.0.4 |
| 2 | lab project under examples/lab24-crm | Pass |
| 3 | GUIDE deliverables and checkpoints complete | Pass, timed path, C1 and C2 N/A |
| 4 | commands succeed | Pass, mvn -B test and spring-boot:run |
| 5 | evidence under notes/screenshots/lab-24 | Pass, kept in the project as since lab 14 |
