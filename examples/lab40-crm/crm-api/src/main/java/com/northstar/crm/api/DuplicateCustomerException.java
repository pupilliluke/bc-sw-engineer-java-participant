package com.northstar.crm.api;

/**
 * Raised when a normalized email is already taken. Step 11 maps this to HTTP
 * 409: the request was valid, it lost a race or duplicated an existing row.
 * Extends RuntimeException so an @Transactional service method rolls back.
 */
public class DuplicateCustomerException extends RuntimeException {

  private final String correlationId;

  public DuplicateCustomerException(String email, String correlationId) {
    super("duplicate customer email " + email + " [" + correlationId + "]");
    this.correlationId = correlationId;
  }

  public String getCorrelationId() {
    return correlationId;
  }
}
