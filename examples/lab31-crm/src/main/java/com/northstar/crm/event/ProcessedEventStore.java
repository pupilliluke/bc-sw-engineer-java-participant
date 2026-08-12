package com.northstar.crm.event;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class ProcessedEventStore {

  private final Set<String> seen = ConcurrentHashMap.newKeySet();

  /**
   * @return true if this is the first time seeing eventId
   *
   * <p>Set.add is the check and the mark in one atomic operation, so two threads
   * handling the same eventId cannot both see it as new. Lab only: this resets on
   * restart and is not shared between instances, so production needs a durable
   * store with a unique constraint on eventId.
   */
  public boolean markIfNew(String eventId) {
    return seen.add(eventId);
  }
}
