package com.northstar.crm.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * A container HEALTHCHECK carries no bearer token, so the probes must answer
 * anonymously. Before the Lab 41 pre-lab fixes these returned 404 (probes not
 * enabled) and 401 (SecurityConfig permitted "/actuator/health" as an exact
 * match, which does not cover the sub-paths) -- and either failure looks
 * identical to a broken application from outside the container.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ProbeEndpointsTest {

  @Autowired MockMvc mockMvc;

  @Test
  @DisplayName("readiness answers anonymously, as a HEALTHCHECK would call it")
  void readinessIsAnonymous() throws Exception {
    mockMvc.perform(get("/actuator/health/readiness"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
  }

  @Test
  @DisplayName("liveness answers anonymously")
  void livenessIsAnonymous() throws Exception {
    mockMvc.perform(get("/actuator/health/liveness"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
  }

  @Test
  @DisplayName("probes expose status only, never component detail")
  void probesDoNotLeakComponentDetail() throws Exception {
    mockMvc.perform(get("/actuator/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.components").doesNotExist());
  }
}
