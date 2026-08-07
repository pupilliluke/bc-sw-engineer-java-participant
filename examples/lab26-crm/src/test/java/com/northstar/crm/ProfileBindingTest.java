package com.northstar.crm;

import com.northstar.crm.config.NorthstarIntegrationProperties;
import com.northstar.crm.model.Customer;
import com.northstar.crm.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class ProfileBindingTest {

  @Autowired
  private NorthstarIntegrationProperties properties;

  @Autowired
  private CustomerService customerService;

  @Test
  void testProfileBindsTimeoutAndCustomerSeed() {
    assertEquals(100, properties.getConnectTimeoutMs());
    assertEquals("http://localhost:9090", properties.getApiBaseUrl());

    Customer amina = customerService.get("CUS-1001");
    assertEquals("CUS-1001", amina.getId());
    assertEquals("Amina Khan", amina.getName());
  }
}
