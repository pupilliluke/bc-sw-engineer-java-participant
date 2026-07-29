package com.northstar.crm.service;

import com.northstar.crm.entity.CustomerStatus;

/**
 * Status-change collaborator extracted from CustomerService in Lab 11.
 *
 * Exists so updateStatus can announce a transition without deciding how it is
 * delivered, and so a test can verify the announcement with a mock instead of
 * capturing stdout. Deliberately one method, no Spring, no Kafka.
 */
public interface CustomerNotifier {

    void notifyStatusChange(String customerId, CustomerStatus oldStatus, CustomerStatus newStatus);
}
