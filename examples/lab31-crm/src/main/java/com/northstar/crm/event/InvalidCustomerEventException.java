package com.northstar.crm.event;

/** Contract violation on a consumed record. Never retryable, the record will not change. */
public class InvalidCustomerEventException extends RuntimeException {

  public InvalidCustomerEventException(String message) {
    super(message);
  }
}
