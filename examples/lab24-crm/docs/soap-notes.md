# Lab 24 — SOAP notes

- `@PayloadRoot` must not re-implement lifecycle rules. The endpoint reads customerId off the request element, calls `customerService.get(id)` and maps the result. Create, seeding and not-found all stay in `CustomerService`, so REST and SOAP cannot answer differently for the same id. CUS-2401 was created over REST and read back over SOAP to prove the store is shared.
- Fault vs REST ErrorResponse: one service exception, two protocol adapters — `IllegalArgumentException` surfaces as a SOAP fault on `/ws` and as a Boot 500 on `/api/customers`, and neither body carries a stack trace, class name or secret.
- Correlation / evidence id: `lab24-001`

## Contract

XSD `src/main/resources/customer.xsd`, namespace `http://northstar.com/crm/customers`,
getCustomer only. WSDL is generated from it at `/ws/customers.wsdl`, port type
`CustomersPort`. The WSDL operation name `GetCustomer` is derived by
`DefaultWsdl11Definition` from the `GetCustomerRequest` / `GetCustomerResponse`
element pair; it is not written anywhere by hand.

The XSD calls the field `customerId` and the Java domain calls it `id`.
`CustomerSoapMapper` bridges the two, so the partner's names are fixed by the
contract and not by the domain class.

## Timed path scope

DOM `Element` mapper, no JAXB or XJC. `spring-ws-security` is not a dependency
and no `Wss4jSecurityInterceptor` is registered, so unsecured
`requests/get-customer.xml` succeeds. Plaintext PasswordText UsernameToken would
be lab-only in any case; production needs TLS underneath plus a digest and
rotated secrets.

Not-found returns faultcode `SOAP-ENV:Server` because
`IllegalArgumentException` is unmapped. `Server` tells the partner to retry a
call that will never succeed, which is what full-path
`SoapFaultMappingExceptionResolver` fixes by mapping not-found to `Client`.
