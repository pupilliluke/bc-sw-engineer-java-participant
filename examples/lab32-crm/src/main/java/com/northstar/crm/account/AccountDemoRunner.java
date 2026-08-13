package com.northstar.crm.account;

import java.util.concurrent.TimeUnit;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "lab.demo", havingValue = "true")
public class AccountDemoRunner implements CommandLineRunner {

  private final AccountProfileService service;

  public AccountDemoRunner(AccountProfileService service) {
    this.service = service;
  }

  @Override
  public void run(String... args) throws Exception {
    for (int i = 0; i < 5; i++) {
      service.find("CUS-1001", "lab-request-001").get(10, TimeUnit.SECONDS);
    }
    Thread.sleep(2500);
    for (int i = 0; i < 2; i++) {
      service.find("CUS-1001", "lab-request-001").get(10, TimeUnit.SECONDS);
    }
  }
}
