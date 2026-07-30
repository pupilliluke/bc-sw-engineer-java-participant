package com.northstar.crm;

import com.northstar.crm.api.CustomerApiFacade;
import com.northstar.crm.dto.CustomerRequestDTO;
import com.northstar.crm.entity.Customer;
import com.northstar.crm.entity.CustomerStatus;
import com.northstar.crm.repository.CustomerRepository;
import com.northstar.crm.repository.InMemoryCustomerRepository;
import com.northstar.crm.service.CustomerService;
import com.northstar.crm.service.CustomerValidator;
import com.northstar.crm.service.DefaultCustomerService;

/**
 * Lab 15 demo. The first four lines are the dependency graph: one repository,
 * shared by the validator and the service, with the facade on top.
 *
 * The seeds go through the facade so shape validation runs first. The
 * transitions call the service directly.
 */
public class Main {

    private static final String CORRELATION_ID = "lab-request-001";

    public static void main(String[] args) {
        CustomerRepository repo = new InMemoryCustomerRepository();
        CustomerValidator validator = new CustomerValidator(repo);
        CustomerService service = new DefaultCustomerService(repo, validator);
        CustomerApiFacade api = new CustomerApiFacade(service);

        System.out.println("create 1001      -> " + api.create(
                request("CUS-1001", "Amina Khan", "amina.khan@example.com", "555-0101", "ACTIVE"),
                CORRELATION_ID));
        System.out.println("create 1002      -> " + api.create(
                request("CUS-1002", "Ravi Singh", "ravi.singh@example.com", "555-0102", "PROSPECT"),
                CORRELATION_ID));

        Customer activated = service.changeStatus("CUS-1002", CustomerStatus.ACTIVE, CORRELATION_ID);
        System.out.printf("activated %s status=%s%n",
                activated.getCustomerId(), activated.getStatus());

        try {
            service.changeStatus("CUS-1001", CustomerStatus.PROSPECT, CORRELATION_ID);
        } catch (IllegalStateException ex) {
            System.out.println("expected failure: " + ex.getMessage());
        }
        System.out.println("CUS-1001 still: "
                + service.findById("CUS-1001").orElseThrow().getStatus());

        // CUS-1003 is a spare fixture, so the CLOSED case leaves CUS-1001 and
        // CUS-1002 as the guide seeds them.
        api.create(request("CUS-1003", "Priya Patel", "priya.patel@example.com", null, "PROSPECT"),
                CORRELATION_ID);

        show("activate twice", () -> api.changeStatus("CUS-1002", "ACTIVE", CORRELATION_ID));
        show("closed is final", () -> {
            service.changeStatus("CUS-1003", CustomerStatus.CLOSED, CORRELATION_ID);
            service.changeStatus("CUS-1003", CustomerStatus.ACTIVE, CORRELATION_ID);
        });
        show("duplicate email", () -> api.create(
                request("CUS-1004", "Sam Okafor", "AMINA.KHAN@example.com", null, "ACTIVE"),
                CORRELATION_ID));
        show("unknown id", () -> api.changeStatus("CUS-9999", "ACTIVE", CORRELATION_ID));
        show("mutate listAll", () -> service.listAll().clear());

        System.out.println("listAll          -> " + service.listAll());
    }

    private static CustomerRequestDTO request(String customerId, String fullName,
                                              String email, String phone, String status) {
        return new CustomerRequestDTO(customerId, fullName, email, phone, status);
    }

    private static void show(String label, Runnable call) {
        try {
            call.run();
            System.out.printf("%-16s -> no failure, check the rule%n", label);
        } catch (Exception ex) {
            System.out.printf("%-16s -> %s: %s%n", label, ex.getClass().getSimpleName(), ex.getMessage());
        }
    }
}
