package com.northstar.crm;

import com.northstar.crm.api.CustomerApiFacade;
import com.northstar.crm.dto.CustomerRequestDTO;
import com.northstar.crm.service.CustomerService;

/**
 * Lab 14 demo — every call goes through the facade, so everything printed as an
 * API response is a CustomerResponseDTO. The five failures at the bottom fail in
 * four different places: bean validation, the mapper's enum lookup, a service
 * rule, and a missing id.
 */
public class Main {

    private static final String CORRELATION_ID = "lab-request-001";

    public static void main(String[] args) {
        CustomerApiFacade api = new CustomerApiFacade(new CustomerService());

        System.out.println("create 1001      -> " + api.create(
                request("CUS-1001", "Amina Khan", "amina.khan@example.com", "555-0101", "ACTIVE"),
                CORRELATION_ID));
        System.out.println("create 1002      -> " + api.create(
                request("CUS-1002", "Ravi Singh", "ravi.singh@example.com", "555-0102", "PROSPECT"),
                CORRELATION_ID));

        System.out.println("get 1001         -> " + api.get("CUS-1001", CORRELATION_ID));
        System.out.println("get 1002         -> " + api.get("CUS-1002", CORRELATION_ID));

        show("invalid email", () -> api.create(
                request("CUS-1003", "Priya Patel", "not-an-email", "555-0103", "ACTIVE"),
                CORRELATION_ID));
        show("blank name", () -> api.create(
                request("CUS-1004", " ", "nobody@example.com", null, "ACTIVE"),
                CORRELATION_ID));
        show("status typo", () -> api.create(
                request("CUS-1005", "Sam Okafor", "sam.okafor@example.com", null, "ACTVE"),
                CORRELATION_ID));
        show("duplicate 1001", () -> api.create(
                request("CUS-1001", "Amina Khan", "amina.khan@example.com", "555-0101", "ACTIVE"),
                CORRELATION_ID));
        show("unknown id", () -> api.get("CUS-9999", CORRELATION_ID));
    }

    private static CustomerRequestDTO request(String customerId, String fullName,
                                              String email, String phone, String status) {
        return new CustomerRequestDTO(customerId, fullName, email, phone, status);
    }

    private static void show(String label, Runnable call) {
        try {
            call.run();
            System.out.printf("%-16s -> no failure, check the payload%n", label);
        } catch (Exception ex) {
            System.out.printf("%-16s -> EX: %s%n", label, ex.getMessage());
        }
    }
}
