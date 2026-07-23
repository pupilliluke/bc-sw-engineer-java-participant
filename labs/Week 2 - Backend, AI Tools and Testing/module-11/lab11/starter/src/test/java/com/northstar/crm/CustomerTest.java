package com.northstar.crm;

import com.northstar.crm.entity.Customer;
import com.northstar.crm.entity.CustomerStatus;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {
    @Test
    void equalsUsesCustomerIdOnly() {
        // TODO: two customers same id different names → assertEquals; different ids → assertNotEquals
        throw new UnsupportedOperationException("TODO: Customer equality test");
    }
}
