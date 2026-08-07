package com.northstar.crm;

import com.northstar.crm.model.Customer;
import com.northstar.crm.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class CrmApplicationTests {

  @Autowired
  private CustomerService customerService;

  @Test
  void contextLoadsAndRestSeedVisible() {
    Customer seeded = customerService.get("CUS-1001");
    assertEquals("Amina Khan", seeded.getName());
    assertEquals("ACTIVE", seeded.getStatus());
  }
}
