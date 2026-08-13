package com.northstar.crm.account;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
class AccountProfileResilienceTest {

  private static WireMockServer wireMock;

  @Autowired
  AccountProfileService service;

  @Autowired
  CircuitBreakerRegistry registry;

  @Autowired
  RetryRegistry retryRegistry;

  @Autowired
  AccountClient client;

  @BeforeAll
  static void startStub() {
    wireMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    wireMock.start();
  }

  @AfterAll
  static void stopStub() {
    wireMock.stop();
  }

  @DynamicPropertySource
  static void accountApi(DynamicPropertyRegistry registry) {
    registry.add("account.api.base-url", () -> "http://localhost:" + wireMock.port());
  }

  @BeforeEach
  void resetState() {
    wireMock.resetAll();
    registry.circuitBreaker("accountProfile").reset();
  }

  private static final String CORRELATION = "lab-request-001";

  private static String okBody(String customerId) {
    return "{\"customerId\":\"" + customerId + "\",\"available\":true,\"note\":\"ok\"}";
  }

  @Test
  void healthyCall_returnsAvailable() throws Exception {
    wireMock.stubFor(get(urlPathMatching("/accounts/CUS-1001/summary"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(okBody("CUS-1001"))));

    AccountSummary summary = service.find("CUS-1001", CORRELATION).get(5, TimeUnit.SECONDS);

    assertThat(summary.customerId()).isEqualTo("CUS-1001");
    assertThat(summary.available()).isTrue();
    assertThat(registry.circuitBreaker("accountProfile").getState())
        .isEqualTo(CircuitBreaker.State.CLOSED);
    wireMock.verify(getRequestedFor(urlPathMatching("/accounts/CUS-1001/summary"))
        .withHeader("X-Correlation-Id", equalTo(CORRELATION)));
  }

  @Test
  void openCircuit_failsFastWithoutHittingStub() throws Exception {
    wireMock.stubFor(get(urlPathMatching("/accounts/CUS-1002/summary"))
        .willReturn(aResponse().withStatus(503)));

    for (int i = 0; i < 5; i++) {
      AccountSummary degraded = service.find("CUS-1002", CORRELATION).get(10, TimeUnit.SECONDS);
      assertThat(degraded.available()).isFalse();
    }

    assertThat(registry.circuitBreaker("accountProfile").getState())
        .isEqualTo(CircuitBreaker.State.OPEN);

    int callsBefore = wireMock.getAllServeEvents().size();
    long start = System.nanoTime();
    AccountSummary summary = service.find("CUS-1002", CORRELATION).get(5, TimeUnit.SECONDS);
    Duration elapsed = Duration.ofNanos(System.nanoTime() - start);

    assertThat(summary.available()).isFalse();
    assertThat(summary.note()).isEqualTo("account-profile-unavailable");
    assertThat(wireMock.getAllServeEvents()).hasSize(callsBefore);
    assertThat(elapsed).isLessThan(Duration.ofMillis(500));
  }

  @Test
  void retryRecovery_secondAttemptSucceeds() {
    wireMock.stubFor(get(urlPathMatching("/accounts/CUS-1001/summary"))
        .inScenario("recovery").whenScenarioStateIs(Scenario.STARTED)
        .willReturn(aResponse().withStatus(503))
        .willSetStateTo("available"));
    wireMock.stubFor(get(urlPathMatching("/accounts/CUS-1001/summary"))
        .inScenario("recovery").whenScenarioStateIs("available")
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(okBody("CUS-1001"))));

    Retry retry = retryRegistry.retry("accountProfile");
    AccountSummary summary =
        Retry.decorateSupplier(retry, () -> client.fetch("CUS-1001", CORRELATION)).get();

    assertThat(summary.available()).isTrue();
    assertThat(summary.customerId()).isEqualTo("CUS-1001");
    wireMock.verify(2, getRequestedFor(urlPathMatching("/accounts/CUS-1001/summary")));
  }

  @Test
  void halfOpen_successfulProbesCloseTheBreaker() throws Exception {
    wireMock.stubFor(get(urlPathMatching("/accounts/CUS-1002/summary"))
        .willReturn(aResponse().withStatus(503)));
    for (int i = 0; i < 5; i++) {
      service.find("CUS-1002", CORRELATION).get(10, TimeUnit.SECONDS);
    }
    CircuitBreaker breaker = registry.circuitBreaker("accountProfile");
    assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

    wireMock.resetAll();
    wireMock.stubFor(get(urlPathMatching("/accounts/CUS-1002/summary"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(okBody("CUS-1002"))));
    Thread.sleep(2100);

    AccountSummary firstProbe = service.find("CUS-1002", CORRELATION).get(10, TimeUnit.SECONDS);
    assertThat(firstProbe.available()).isTrue();
    assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.HALF_OPEN);

    AccountSummary secondProbe = service.find("CUS-1002", CORRELATION).get(10, TimeUnit.SECONDS);
    assertThat(secondProbe.available()).isTrue();
    assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
  }

  @Test
  void timeout_returnsUnavailableFallback() throws Exception {
    wireMock.stubFor(get(urlPathMatching("/accounts/CUS-1001/summary"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(okBody("CUS-1001"))
            .withFixedDelay(4000)));

    long start = System.nanoTime();
    AccountSummary summary = service.find("CUS-1001", CORRELATION).get(20, TimeUnit.SECONDS);
    Duration elapsed = Duration.ofNanos(System.nanoTime() - start);

    assertThat(summary.available()).isFalse();
    assertThat(summary.note()).isEqualTo("account-profile-unavailable");
    assertThat(elapsed).isLessThan(Duration.ofSeconds(4));
    wireMock.verify(getRequestedFor(urlPathMatching("/accounts/CUS-1001/summary")));
  }
}
