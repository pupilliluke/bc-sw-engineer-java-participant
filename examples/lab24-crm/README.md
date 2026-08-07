Northstar CRM build (Lab 24)

  mvn -B test
  mvn -B spring-boot:run
  # WSDL: http://localhost:8080/ws/customers.wsdl

  curl -s http://localhost:8080/ws/customers.wsdl | grep "wsdl:operation"
  curl -s -X POST http://localhost:8080/ws \
    -H "Content-Type: text/xml; charset=utf-8" \
    --data @requests/get-customer.xml
  curl -s http://localhost:8080/api/customers/CUS-1001

Copied from the lab 24 starter, not carried forward from lab23-crm. Spring-WS
4.0.11 and wsdl4j 1.6.3 come from spring-boot-starter-web-services under the
Boot 3.3.5 parent. MessageDispatcherServlet is registered at /ws/* beside the
Boot DispatcherServlet at /, so REST and SOAP run in the same app on 8080.

Timed path as the guide scopes it: contract-first customer.xsd with getCustomer
only, DOM Element mapper, one @PayloadRoot, port type CustomersPort, REST kept.
No JAXB or XJC. spring-ws-security is not a dependency and no
Wss4jSecurityInterceptor is registered, so unsecured requests succeed.
requests/get-customer-not-found.xml is mine. The guide lists it under the full
path, but experiment 2 needs a CUS-9999 payload on the timed path too.

TIMED-PATH PASS CRITERIA

| Criterion | Result |
| --------- | ------ |
| WSDL reachable (or config compiles) | Pass, /ws/customers.wsdl serves portType CustomersPort and operation GetCustomer |
| @PayloadRoot getCustomer delegates to CustomerService | Pass, three-line method, no rules in the endpoint |
| REST /api/customers still works for CUS-1001 | Pass, 200 with Amina Khan ACTIVE |
| Sample XML under requests/ reviewed | Pass, get-customer.xml plus get-customer-not-found.xml for experiment 2 |

SECURITY NOTES

untrusted: the SOAP body, the customerId element, the JSON body on the REST
side, and any caller of /ws or /actuator. Nothing validates customerId beyond
the mapper checking the element exists; the service decides what is found.

authn/authz: none. /ws is open, UsernameToken is not wired, and PasswordText
would put the password on the wire in plaintext anyway. Production needs TLS
plus a digest and rotated secrets. Lab 28 covers REST security.

sensitive: name, email, status. The fault body carries the message
"Customer not found: CUS-9999" and no stack trace or class name.

CLEANUP

  mvn -B clean
  git status

Ctrl+C spring-boot:run. target/ is ignored. Keep lab24-crm, lab 25 refactors the
layering under the same service contract.

NOTES

Evidence and the failure experiments are in notes/screenshots/lab-24/.
Checkpoints and reflection answers are in notes/Week 3/Module 24/lab24-answers.md.
The contract notes are docs/soap-notes.md. Full GUIDE at
labs/Week 3 - Spring Framework and Enterprise Patterns/module-24/lab24/.
