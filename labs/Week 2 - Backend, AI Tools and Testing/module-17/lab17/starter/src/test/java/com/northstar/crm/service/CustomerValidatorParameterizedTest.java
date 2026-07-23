package com.northstar.crm.service;

import com.northstar.crm.entity.CustomerStatus;
import com.northstar.crm.repository.InMemoryCustomerRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

class CustomerValidatorParameterizedTest {
    CustomerValidator validator = new CustomerValidator(new InMemoryCustomerRepository());

    @ParameterizedTest
    @CsvSource({
            // TODO: legal rows e.g. PROSPECT,ACTIVE
            "PROSPECT,ACTIVE"
    })
    void legalTransitions(CustomerStatus from, CustomerStatus to) {
        // TODO: assertDoesNotThrow validateTransition(from, to, "lab-request-001")
        throw new UnsupportedOperationException("TODO: legal parameterized");
    }

    @ParameterizedTest
    @CsvSource({
            // TODO: illegal rows e.g. ACTIVE,PROSPECT and CLOSED,ACTIVE
            "ACTIVE,PROSPECT"
    })
    void illegalTransitions(CustomerStatus from, CustomerStatus to) {
        // TODO: assertThrows BusinessException
        throw new UnsupportedOperationException("TODO: illegal parameterized");
    }
}
