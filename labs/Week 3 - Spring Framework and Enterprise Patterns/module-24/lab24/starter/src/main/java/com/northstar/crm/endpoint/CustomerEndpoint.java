package com.northstar.crm.endpoint;

import com.northstar.crm.service.CustomerService;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;

// TODO: ensure @Endpoint is present (stereotype for Spring-WS)
@Endpoint
public class CustomerEndpoint {
  private static final String NAMESPACE = "http://northstar.com/crm/customers";

  private final CustomerService customerService;
  private final CustomerSoapMapper mapper;

  public CustomerEndpoint(CustomerService customerService, CustomerSoapMapper mapper) {
    this.customerService = customerService;
    this.mapper = mapper;
  }

  // TODO: @PayloadRoot(namespace = NAMESPACE, localPart = "GetCustomerRequest")
  // TODO: @ResponsePayload
  public Object getCustomer(@RequestPayload Object request) {
    // TODO: extract customerId via mapper; call customerService.get; map to response
    throw new UnsupportedOperationException("TODO: implement getCustomer payload handling");
  }
}
