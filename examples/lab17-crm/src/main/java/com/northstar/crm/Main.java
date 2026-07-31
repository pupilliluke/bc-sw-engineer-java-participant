package com.northstar.crm;

import com.northstar.crm.api.ApiResult;
import com.northstar.crm.api.CustomerApiFacade;
import com.northstar.crm.dto.CustomerRequestDTO;
import com.northstar.crm.repository.CustomerRepository;
import com.northstar.crm.repository.InMemoryCustomerRepository;
import com.northstar.crm.service.CustomerService;
import com.northstar.crm.service.CustomerValidator;
import com.northstar.crm.service.DefaultCustomerService;

/**
 * Lab 16 demo. The wiring is Lab 15's, with the handler behind the facade.
 *
 * Nothing here catches an exception. Every call returns Ok or Fail, and the
 * three graded lines are the 400, the 404 and the 409 JSON.
 */
public class Main {

    private static final String CORRELATION_ID = "lab-request-001";

    public static void main(String[] args) {
        CustomerRepository repo = new InMemoryCustomerRepository();
        CustomerValidator validator = new CustomerValidator(repo);
        CustomerService service = new DefaultCustomerService(repo, validator);
        CustomerApiFacade api = new CustomerApiFacade(service);

        show("create 1001", api.create(
                request("CUS-1001", "Amina Khan", "amina.khan@example.com", "555-0101", "ACTIVE"),
                CORRELATION_ID));
        show("create 1002", api.create(
                request("CUS-1002", "Ravi Singh", "ravi.singh@example.com", "555-0102", "PROSPECT"),
                CORRELATION_ID));
        show("activate 1002", api.changeStatus("CUS-1002", "ACTIVE", CORRELATION_ID));

        show("400 bad email", api.create(
                request("CUS-1003", "Priya Patel", "not-an-email", null, "PROSPECT"),
                CORRELATION_ID));
        show("404 unknown", api.getById("CUS-9999", CORRELATION_ID));
        show("409 transition", api.changeStatus("CUS-1001", "PROSPECT", CORRELATION_ID));

        show("1001 still", api.getById("CUS-1001", CORRELATION_ID));
        show("409 duplicate", api.create(
                request("CUS-1004", "Sam Okafor", "AMINA.KHAN@example.com", null, "ACTIVE"),
                CORRELATION_ID));
        show("400 bad status", api.changeStatus("CUS-1002", "ACTVE", CORRELATION_ID));
    }

    private static CustomerRequestDTO request(String customerId, String fullName,
                                              String email, String phone, String status) {
        return new CustomerRequestDTO(customerId, fullName, email, phone, status);
    }

    private static void show(String label, ApiResult result) {
        String line = switch (result) {
            case ApiResult.Ok ok -> "ok    " + ok.body();
            case ApiResult.Fail fail -> "fail  " + fail.error().toJson();
        };
        System.out.printf("%-14s -> %s%n", label, line);
    }
}
