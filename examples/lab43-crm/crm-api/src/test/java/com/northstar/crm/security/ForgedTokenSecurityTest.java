package com.northstar.crm.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ForgedTokenSecurityTest {

    @Autowired MockMvc mockMvc;

    @Test
    void forgedAdminTokenIsRejected() throws Exception {
        // 1. Build an attacker's signer. NOT @Autowired -- construct one with the
        //    secret that is committed in application.yml:34.
        JwtService attackerSigner = new JwtService("lab-only-change-me");

        // 2. Mint a token claiming whatever role you like.
        String forged = attackerSigner.issueToken("attacker", "ADMIN");

        // 3. Send it to the admin endpoint and assert the status you chose in (d).
        mockMvc.perform(get("/api/admin/ping")
                        .header("Authorization", "Bearer " + forged)
                        .header("X-Correlation-Id", "lab-request-001"))
                .andExpect(status().isUnauthorized());

    }
}