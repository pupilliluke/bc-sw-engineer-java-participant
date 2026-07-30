package com.northstar.crm;

import com.northstar.crm.service.CustomerService;
import com.northstar.crm.entity.CustomerStatus;

/**
 * AFTER demo — walks the refactored API: create CUS-1001 / CUS-1002, get by id,
 * a non-interned id lookup that the frozen get() would have missed, then the
 * duplicate, blank and unknown failures, each carrying the correlation id.
 */
public class Main {
    public static void main(String[] args) {
        CustomerService service = new CustomerService();

        service.createCustomer("CUS-1001", "Amina Khan", "amina.khan@example.com", "555-0101", CustomerStatus.ACTIVE);
        service.createCustomer("CUS-1002", "Ravi Singh", "ravi.singh@example.com", "555-0102", CustomerStatus.PROSPECT);

        System.out.println("get literal      -> " + service.getCustomer("CUS-1001"));
        System.out.println("get new String   -> " + service.getCustomer(new String("CUS-1001")));
        try {
            System.out.println("duplicate 1001   -> " + service.createCustomer("CUS-1001", "Amina Khan",
                    "amina.khan@example.com", "555-0101", CustomerStatus.ACTIVE));
        } catch (Exception ex) {
            System.out.println("duplicate 1001   -> EX: " + ex.getMessage());
        }
        try {
            System.out.println("blank id         -> " + service.createCustomer("", "Nobody",
                    "nobody@example.com", null, CustomerStatus.ACTIVE));
        } catch (Exception ex) {
            System.out.println("blank id         -> EX: " + ex.getMessage());
        }
        System.out.println("activate 1002    -> "
                + service.updateStatus("CUS-1002", CustomerStatus.ACTIVE));
        System.out.println("null status      -> defaults to PROSPECT: " + service.createCustomer(
                "CUS-1003", "Priya Patel", "priya.patel@example.com", "555-0103", null));
        try {
            System.out.println("unknown id       -> " + service.getCustomer("CUS-9999"));
        } catch (Exception ex) {
            System.out.println("unknown id       -> EX: " + ex.getMessage());
        }
    }
}
