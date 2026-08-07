package com.northstar.crm.endpoint;

import com.northstar.crm.model.Customer;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

@Component
public class CustomerSoapMapper {
  static final String NAMESPACE = "http://northstar.com/crm/customers";

  public String customerIdFromGetRequest(Element request) {
    NodeList found = request.getElementsByTagNameNS(NAMESPACE, "customerId");
    if (found.getLength() == 0) {
      throw new IllegalArgumentException("customerId missing from GetCustomerRequest");
    }
    return found.item(0).getTextContent().trim();
  }

  public Element toGetCustomerResponse(Customer customer) {
    Document document = newDocument();
    Element response = document.createElementNS(NAMESPACE, "GetCustomerResponse");
    document.appendChild(response);
    appendChild(document, response, "customerId", customer.getId());
    appendChild(document, response, "name", customer.getName());
    appendChild(document, response, "email", customer.getEmail());
    appendChild(document, response, "status", customer.getStatus());
    return response;
  }

  private void appendChild(Document document, Element parent, String name, String value) {
    Element child = document.createElementNS(NAMESPACE, name);
    child.setTextContent(value);
    parent.appendChild(child);
  }

  private Document newDocument() {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    try {
      return factory.newDocumentBuilder().newDocument();
    } catch (ParserConfigurationException e) {
      throw new IllegalStateException("Cannot create DOM document", e);
    }
  }
}
