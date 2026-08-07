package com.northstar.crm.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class ProdSecretsCheck {
  private final NorthstarIntegrationProperties properties;
  private final Environment environment;

  public ProdSecretsCheck(NorthstarIntegrationProperties properties, Environment environment) {
    this.properties = properties;
    this.environment = environment;
  }

  @PostConstruct
  void requireSecretsFromEnvironment() {
    require("northstar.integration.api-key", properties.getApiKey());
    require("spring.datasource.username", environment.getProperty("spring.datasource.username"));
    require("spring.datasource.password", environment.getProperty("spring.datasource.password"));
  }

  private static void require(String key, String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalStateException(key + " is required under the prod profile and was not set");
    }
    if (value.startsWith("${") && value.endsWith("}")) {
      throw new IllegalStateException(
          key + " still holds the unresolved placeholder " + value
              + "; set it from the environment before starting prod");
    }
  }
}
