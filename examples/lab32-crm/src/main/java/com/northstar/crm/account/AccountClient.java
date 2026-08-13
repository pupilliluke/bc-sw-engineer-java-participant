package com.northstar.crm.account;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class AccountClient {

  static final String CORRELATION_HEADER = "X-Correlation-Id";

  private final RestClient restClient;

  public AccountClient(@Value("${account.api.base-url}") String baseUrl) {
    this.restClient = RestClient.builder().baseUrl(baseUrl).build();
  }

  public AccountSummary fetch(String customerId, String correlationId) {
    try {
      return restClient.get()
          .uri("/accounts/{customerId}/summary", customerId)
          .header(CORRELATION_HEADER, correlationId)
          .retrieve()
          .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
            throw new TemporaryAccountException(
                "account api " + response.getStatusCode() + " for " + customerId);
          })
          .body(AccountSummary.class);
    } catch (ResourceAccessException e) {
      throw new TemporaryAccountException(
          "account api unreachable for " + customerId + ": " + e.getMessage());
    }
  }
}
