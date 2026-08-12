package com.northstar.crm.event;

public class UnsupportedEventVersionException extends RuntimeException {

  public UnsupportedEventVersionException(int eventVersion) {
    super("Unsupported eventVersion: " + eventVersion);
  }
}
