package com.northstar.crm.controller;

import com.northstar.crm.security.JwtService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;
  private final PasswordEncoder passwordEncoder;

  public AuthController(JwtService jwtService, UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
    this.jwtService = jwtService;
    this.userDetailsService = userDetailsService;
    this.passwordEncoder = passwordEncoder;
  }

  @PostMapping("/login")
  public Map<String, String> login(@RequestBody Map<String, String> body) {
    String username = body.get("username");
    String password = body.get("password");

    UserDetails user = userDetailsService.loadUserByUsername(username);
    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials");
    }

    String role = user.getAuthorities().stream()
        .map(auth -> auth.getAuthority().replace("ROLE_", ""))
        .findFirst()
        .orElse("AGENT");

    String token = jwtService.issueToken(username, role);
    return Map.of("accessToken", token, "tokenType", "Bearer");
  }
}
