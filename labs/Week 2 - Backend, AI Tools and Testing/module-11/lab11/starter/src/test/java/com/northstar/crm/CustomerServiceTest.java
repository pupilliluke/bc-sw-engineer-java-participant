package com.northstar.crm;

import com.northstar.crm.entity.Customer;
import com.northstar.crm.entity.CustomerStatus;
import com.northstar.crm.service.CustomerService;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class CustomerServiceTest {
    @Test
    void addAndFindAminaKhan() {
        // TODO: add CUS-1001 ACTIVE; assert findByCustomerId present
        throw new UnsupportedOperationException("TODO: service happy path");
    }

    @Test
    void duplicateIdThrows() {
        // TODO: second add with CUS-1001 throws IllegalStateException
        throw new UnsupportedOperationException("TODO: duplicate rejection");
    }

    // TODO: add a Mockito test verifying CustomerNotifier.notifyCreated when wired
}
