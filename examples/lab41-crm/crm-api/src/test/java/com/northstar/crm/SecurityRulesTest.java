package com.northstar.crm;

import com.northstar.crm.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityRulesTest {

  @Autowired MockMvc mockMvc;
  @Autowired JwtService jwtService;

  @Test
  void anonymousListIsUnauthorized() throws Exception {
    mockMvc.perform(get("/api/customers"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void agentTokenCanListCustomers() throws Exception {
    mockMvc.perform(get("/api/customers")
            .header("Authorization", "Bearer " + jwtService.issueToken("agent1", "AGENT"))
            .header("X-Correlation-Id", "lab-request-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].id", containsInAnyOrder("CUS-1001", "CUS-1002")));
  }



    @Test
  void agentTokenOnAdminEndpointIsForbidden() throws Exception {
    mockMvc.perform(get("/api/admin/ping")
            .header("Authorization", "Bearer " + jwtService.issueToken("agent1", "AGENT")))
        .andExpect(status().isForbidden());
  }

  @Test
  void tamperedTokenIsUnauthorized() throws Exception {
    mockMvc.perform(get("/api/customers")
            .header("Authorization", "Bearer lab.agent1.ADMIN.deadbeef"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void roleEscalatedTokenIsRejected() throws Exception {
    String agentToken = jwtService.issueToken("agent1", "AGENT");
    String signature = agentToken.substring(agentToken.lastIndexOf('.') + 1);
    mockMvc.perform(get("/api/admin/ping")
            .header("Authorization", "Bearer lab.agent1.ADMIN." + signature))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void badCredentialsAreUnauthorized() throws Exception {
    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"agent1\",\"password\":\"wrong\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void loginIssuesTokenForDemoAgent() throws Exception {
    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"agent1\",\"password\":\"agent1\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tokenType").value("Bearer"))
        .andExpect(jsonPath("$.role").value("AGENT"));
  }

  @Test
  void securityHeadersArePresent() throws Exception {
    mockMvc.perform(get("/api/customers")
            .header("Authorization", "Bearer " + jwtService.issueToken("agent1", "AGENT")))
        .andExpect(header().string("Content-Security-Policy",
            "default-src 'self'; object-src 'none'; frame-ancestors 'none'; base-uri 'self'"))
        .andExpect(header().string("X-Content-Type-Options", "nosniff"))
        .andExpect(header().string("Referrer-Policy", "no-referrer"))
        .andExpect(header().string("X-Frame-Options", "DENY"));
  }


}
