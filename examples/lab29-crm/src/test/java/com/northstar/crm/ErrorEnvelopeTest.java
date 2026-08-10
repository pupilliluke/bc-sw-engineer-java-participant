package com.northstar.crm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ErrorEnvelopeTest {
  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;

  private String agentToken() throws Exception {
    String body = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"agent1\",\"password\":\"agent1\"}"))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    return objectMapper.readTree(body).get("accessToken").asText();
  }

  @Test
  void validationReturns400Envelope() throws Exception {
    mockMvc.perform(post("/api/customers")
            .header("Authorization", "Bearer " + agentToken())
            .header("X-Correlation-Id", "lab-request-001")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\":\"\",\"name\":\"\",\"email\":\"not-an-email\",\"status\":\"ACTIVE\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.correlationId").value("lab-request-001"))
        .andExpect(jsonPath("$.violations").isArray())
        .andExpect(jsonPath("$.violations.length()").value(greaterThanOrEqualTo(3)))
        .andExpect(jsonPath("$.violations[*].field").value(hasItem("email")))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void missingCustomerReturns404Envelope() throws Exception {
    mockMvc.perform(get("/api/customers/CUS-9999")
            .header("Authorization", "Bearer " + agentToken())
            .header("X-Correlation-Id", "lab-request-001"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value(containsString("CUS-9999")))
        .andExpect(jsonPath("$.correlationId").value("lab-request-001"));
  }

  @Test
  void duplicateReturns409Envelope() throws Exception {
    mockMvc.perform(post("/api/customers")
            .header("Authorization", "Bearer " + agentToken())
            .header("X-Correlation-Id", "lab-request-001")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\":\"CUS-1001\",\"name\":\"Amina Khan\",\"email\":\"amina.khan@example.com\",\"status\":\"ACTIVE\"}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.error").value("Conflict"))
        .andExpect(jsonPath("$.message").value(containsString("CUS-1001")))
        .andExpect(jsonPath("$.correlationId").value("lab-request-001"));
  }

  @Test
  void securityStillRequiresToken() throws Exception {
    mockMvc.perform(get("/api/customers/CUS-1001"))
        .andExpect(status().isUnauthorized());
  }
}
