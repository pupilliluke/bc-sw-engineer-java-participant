package com.northstar.crm.account;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AccountProfileService {

  private static final Logger log = LoggerFactory.getLogger(AccountProfileService.class);

  private final AccountClient client;

  public AccountProfileService(AccountClient client) {
    this.client = client;
  }

  @CircuitBreaker(name = "accountProfile", fallbackMethod = "fallback")
  @Retry(name = "accountProfile")
  @TimeLimiter(name = "accountProfile")
  public CompletableFuture<AccountSummary> find(String customerId, String correlationId) {
    return CompletableFuture.supplyAsync(() -> client.fetch(customerId, correlationId));
  }

  @SuppressWarnings("unused")
  private CompletableFuture<AccountSummary> fallback(
      String customerId, String correlationId, Throwable cause) {
    log.warn("account_profile_degraded customerId={} correlationId={} cause={}",
        customerId, correlationId, cause.getClass().getSimpleName());
    return CompletableFuture.completedFuture(AccountSummary.unavailable(customerId));
  }
}
