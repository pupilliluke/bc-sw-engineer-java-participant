# Lab 24 — Contract-First Recall

## Order (fill)
1. author customer.xsd
2. mvn generate-sources, jaxb2-maven-plugin generates the JAXB request/response types
3. implement @Endpoint with @PayloadRoot, delegate to CustomerService
4. DefaultWsdl11Definition serves the wsdl from the same xsd

## Source of truth
customer.xsd. the lab 13 customer operations carried over to spring-ws. the JAXB
types and the wsdl are both derived from it, neither is hand-edited.

## Why partners care
the billing partner binds to xml names like getCustomerRequest and customerId.
those live in the xsd, so a rename there shows up in the wsdl they read. a change
made only in java never reaches them.

## Scope
Pre-lab only.

## Debug / design challenge

If someone adds a Java field without updating the XSD, what breaks for SOAP clients?

nothing shows up for them. the field is not in the xsd, so it is not in the
generated types or the wsdl, and it never marshals.

## Predict the Output / Behavior

Is code-first WSDL export the Lab 24 primary approach?

no. contract-first, the wsdl is generated from the xsd.

## Pass criteria

Self-check before marking Pass:

- [x] File exists at `notes/contract-first.md`
- [x] Order listed
- [x] XSD as source of truth
- [x] Partner reason
