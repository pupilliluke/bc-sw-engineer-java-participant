# Lab 24 — PayloadRoot Skeleton

## Class annotation
@Endpoint on CustomerEndpoint, CustomerService injected through the constructor

## @PayloadRoot localPart
getCustomerRequest, with namespace http://northstar.com/crm/customer. both have
to match customer.xsd exactly.

## Method inputs/outputs
in: @RequestPayload GetCustomerRequest
out: GetCustomerResponse, method marked @ResponsePayload

## Delegation line (words)
read the customerId off the request, call service.get(id), hand the returned
Customer to CustomerSoapMapper for the response. no rules in between.

## Scope
Pre-lab only.

## Debug / design challenge

If localPart is GetCustomer but XSD says GetCustomerRequest, what happens?

the dispatcher finds no mapping for that root element, the method never runs and
the client gets a fault instead of a response.

## Predict the Output / Behavior

Does @PayloadRoot replace the need for MessageDispatcherServlet config?

no. the servlet has to be registered first, @PayloadRoot only routes inside it.

## Pass criteria

Self-check before marking Pass:

- [x] File exists at `notes/lab24-payloadroot-skeleton.md`
- [x] @Endpoint noted
- [x] localPart noted
- [x] Service delegation
