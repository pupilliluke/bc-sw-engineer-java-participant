package com.northstar.crm.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

@Service
public class JwtService {
  private final String secret;

  public JwtService(@Value("${northstar.security.jwt-secret}") String secret) {
    this.secret = secret;
  }

  public String issueToken(String subject, String role) {
    if (subject == null || subject.isBlank() || role == null || role.isBlank()) {
      throw new IllegalArgumentException("subject and role required");
    }
    String claims = subject + "." + role;
    return "lab." + claims + "." + sign(claims);
  }

  public String parseSubject(String token) {
    return requireLabToken(token)[1];
  }

  public String parseRole(String token) {
    return requireLabToken(token)[2];
  }

  private String[] requireLabToken(String token) {
    if (token == null || token.isBlank()) {
      throw new IllegalArgumentException("missing token");
    }
    String[] parts = token.split("\\.");
    if (parts.length != 4 || !"lab".equals(parts[0])) {
      throw new IllegalArgumentException("invalid token");
    }
    // The signature covers the claims, not just the secret. Lab 29's stub hashed
    // the secret alone, so any holder of one valid token could edit the role
    // segment, keep the signature and be served as ADMIN.
    if (!constantTimeEquals(sign(parts[1] + "." + parts[2]), parts[3])) {
      throw new IllegalArgumentException("invalid token signature");
    }
    return parts;
  }

  private String sign(String claims) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      return Base64.getUrlEncoder()
          .withoutPadding()
          .encodeToString(mac.doFinal(claims.getBytes(StandardCharsets.UTF_8)));
    } catch (GeneralSecurityException ex) {
      throw new IllegalStateException("cannot sign lab token", ex);
    }
  }

  private boolean constantTimeEquals(String expected, String actual) {
    return java.security.MessageDigest.isEqual(
        expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8));
  }
}
