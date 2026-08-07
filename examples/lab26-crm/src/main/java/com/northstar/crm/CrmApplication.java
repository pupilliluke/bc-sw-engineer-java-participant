package com.northstar.crm;

import com.northstar.crm.config.NorthstarIntegrationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@SpringBootApplication
@EnableConfigurationProperties(NorthstarIntegrationProperties.class)
public class CrmApplication {
  private static final Logger log = LoggerFactory.getLogger(CrmApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(CrmApplication.class, args);
  }

  @Bean
  ApplicationRunner logIntegrationConfig(NorthstarIntegrationProperties properties, Environment environment) {
    return args -> log.info("profiles={} apiBaseUrl={} connectTimeoutMs={} apiKeySet={}",
        Arrays.toString(environment.getActiveProfiles()),
        properties.getApiBaseUrl(),
        properties.getConnectTimeoutMs(),
        properties.getApiKey() != null && !properties.getApiKey().isBlank());
  }
}
