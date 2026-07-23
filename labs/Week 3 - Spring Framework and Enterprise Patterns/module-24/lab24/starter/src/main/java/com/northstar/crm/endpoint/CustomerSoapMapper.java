package com.northstar.crm.endpoint;

import com.northstar.crm.model.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerSoapMapper {

  public String customerIdFromGetRequest(Object request) {
    // TODO: read customerId from GetCustomerRequest (or XML Element for lab stub)
    throw new UnsupportedOperationException("TODO: extract customerId");
  }

  public Object toGetCustomerResponse(Customer customer) {
    // TODO: build GetCustomerResponse from domain Customer
    throw new UnsupportedOperationException("TODO: map Customer → SOAP response");
  }
}
