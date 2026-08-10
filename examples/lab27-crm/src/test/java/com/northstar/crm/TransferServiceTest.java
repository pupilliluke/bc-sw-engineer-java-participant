package com.northstar.crm;

import com.northstar.crm.account.AccountRepository;
import com.northstar.crm.account.TransactionLogRepository;
import com.northstar.crm.service.TransferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TransferServiceTest {
  @Autowired TransferService transferService;
  @Autowired AccountRepository accounts;
  @Autowired TransactionLogRepository logs;

  private BigDecimal balanceOf(String id) {
    return accounts.findById(id).orElseThrow().getBalance();
  }

  @Test
  void forceFailRollsBack() {
    BigDecimal before = balanceOf("ACC-MAIN-1001");
    long logsBefore = logs.count();

    assertThrows(Exception.class, () ->
        transferService.transfer("ACC-MAIN-1001", "ACC-FORCE-FAIL", new BigDecimal("10.00")));

    assertEquals(0, before.compareTo(balanceOf("ACC-MAIN-1001")));
    assertEquals(logsBefore, logs.count());
  }

  @Test
  void happyPathMovesFunds() {
    BigDecimal mainBefore = balanceOf("ACC-MAIN-1001");
    BigDecimal loyaltyBefore = balanceOf("ACC-LOYALTY-1001");
    long logsBefore = logs.count();
    BigDecimal amount = new BigDecimal("5.00");

    transferService.transfer("ACC-MAIN-1001", "ACC-LOYALTY-1001", amount);

    assertEquals(0, mainBefore.subtract(amount).compareTo(balanceOf("ACC-MAIN-1001")));
    assertEquals(0, loyaltyBefore.add(amount).compareTo(balanceOf("ACC-LOYALTY-1001")));
    assertEquals(logsBefore + 1, logs.count());
  }
}
