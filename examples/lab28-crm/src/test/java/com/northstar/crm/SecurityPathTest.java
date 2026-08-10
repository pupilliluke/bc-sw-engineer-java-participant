package com.northstar.crm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityPathTest {
  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;

  private String tokenFor(String username, String password) throws Exception {
    String body = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    return objectMapper.readTree(body).get("accessToken").asText();
  }

  @Test
  void missingTokenIs401() throws Exception {
    mockMvc.perform(get("/api/customers/CUS-1001"))
        .andExpect(status().isUnauthorized());

    mockMvc.perform(get("/api/customers/CUS-1001")
            .header("Authorization", "Bearer lab.agent1.ADMIN.deadbeef"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void agentCanReadCustomerButNotAdmin() throws Exception {
    String token = tokenFor("agent1", "agent1");

    mockMvc.perform(get("/api/customers/CUS-1001")
            .header("Authorization", "Bearer " + token)
            .header("X-Correlation-Id", "lab-request-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Amina Khan"))
        .andExpect(jsonPath("$.status").value("ACTIVE"));

    mockMvc.perform(get("/api/admin/ping")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminCanPing() throws Exception {
    String token = tokenFor("admin1", "admin1");

    mockMvc.perform(get("/api/admin/ping")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.role").value("ADMIN"))
        .andExpect(jsonPath("$.ok").value("true"));
  }
}
