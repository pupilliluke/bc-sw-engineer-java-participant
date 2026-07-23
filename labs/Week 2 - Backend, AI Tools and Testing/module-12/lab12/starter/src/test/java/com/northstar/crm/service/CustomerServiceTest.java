package com.northstar.crm.service;

import com.northstar.crm.entity.Customer;
import com.northstar.crm.entity.CustomerStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Characterization / target-API tests — fail until refactor completes. */
class CustomerServiceTest {

    @Test
    void createAminaKhanThenGetById() {
        // TODO: svc.createCustomer("CUS-1001", "Amina Khan", "amina.khan@example.com", null, ACTIVE)
        // TODO: assert getCustomer returns same id
        throw new UnsupportedOperationException("TODO: target API test");
    }

    @Test
    void unknownIdThrows() {
        // TODO: getCustomer("CUS-9999") throws clearly (not null)
        throw new UnsupportedOperationException("TODO: not-found test");
    }

    @Test
    void duplicateIdThrows() {
        // TODO: second create CUS-1001 throws
        throw new UnsupportedOperationException("TODO: duplicate test");
    }
}
