package com.northstar.crm.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final String secret;

  public JwtService(@Value("${northstar.security.jwt-secret}") String secret) {
    this.secret = secret;
  }

  public String issueToken(String subject, String role) {
    String token = "lab." + subject + "." + role + "." + Integer.toHexString(secret.hashCode());

    return token;
  }

  private String[] validateAndParseParts(String token) {
    if (token == null || !token.startsWith("lab.")) {
      throw new IllegalArgumentException("Invalid token format");
    }

    String[] parts = token.split("\\.");
    if (parts.length != 4) {
      throw new IllegalArgumentException("Invalid token format: expected 4 parts");
    }

    // Validate the signature (last part should contain toHexString(secret.hashCode()))
    String expectedSignature = Integer.toHexString(secret.hashCode());
    if (!parts[3].equals(expectedSignature)) {
      throw new IllegalArgumentException("Invalid token signature");
    }

    return parts;
  }

  public String parseSubject(String token) {
    String[] parts = validateAndParseParts(token);
    return parts[1];
  }

  public String parseRole(String token) {
    String[] parts = validateAndParseParts(token);
    return parts[2];
  }
}
