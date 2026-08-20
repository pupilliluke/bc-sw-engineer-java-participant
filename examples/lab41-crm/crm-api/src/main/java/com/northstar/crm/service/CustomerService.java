package com.northstar.crm.service;

import com.northstar.crm.api.CreateCustomerRequest;
import com.northstar.crm.api.CustomerResponse;
import com.northstar.crm.api.DuplicateCustomerException;
import com.northstar.crm.api.FieldValidationException;
import com.northstar.crm.repository.CustomerEntity;
import com.northstar.crm.repository.CustomerRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

  private static final Map<String, Set<String>> ALLOWED = Map.of(
      "PROSPECT", Set.of("ACTIVE", "CLOSED"),
      "ACTIVE", Set.of("SUSPENDED", "CLOSED"),
      "SUSPENDED", Set.of("ACTIVE", "CLOSED"),
      "CLOSED", Set.of());

  private final CustomerRepository repository;
  private final CustomerMapper mapper;

  public CustomerService(CustomerRepository repository, CustomerMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Transactional
  public CustomerResponse create(CreateCustomerRequest request, String correlationId) {
    String email = normalize(request.email());
    validate(request, email);
    if (repository.existsByEmail(email)) {
      throw new DuplicateCustomerException(email, correlationId);
    }
    CustomerEntity saved = repository.save(mapper.toEntity(request, email));
    return mapper.toResponse(saved);
  }

  @Transactional
  public CustomerResponse update(String id, CreateCustomerRequest incoming, String correlationId) {
    String email = normalize(incoming.email());
    validate(incoming, email);
    CustomerEntity entity = load(id);
    entity.setFullName(incoming.name());
    entity.setEmail(email);
    entity.setStatus(incoming.status());
    return mapper.toResponse(repository.save(entity));
  }

  @Transactional
  public CustomerResponse updateStatus(String id, String newStatus, String correlationId) {
    if (newStatus == null || newStatus.isBlank()) {
      throw new FieldValidationException(Map.of("status", "newStatus is required"));
    }
    CustomerEntity entity = load(id);
    String from = entity.getStatus();
    if (!ALLOWED.getOrDefault(from, Set.of()).contains(newStatus)) {
      throw new IllegalStateException(
          "illegal status transition " + from + " -> " + newStatus + " [" + correlationId + "]");
    }
    entity.setStatus(newStatus);
    return mapper.toResponse(repository.save(entity));
  }

  @Transactional(readOnly = true)
  public CustomerResponse get(String id) {
    return mapper.toResponse(load(id));
  }

  // Unbounded. Step 10 replaces this with findByStatus(status, Pageable); it is
  // left here only so the existing GET /api/customers keeps compiling.
  @Transactional(readOnly = true)
  public List<CustomerResponse> list() {
    return repository.findAll().stream().map(mapper::toResponse).toList();
  }

  // Step 10. Page carries the content slice plus totalElements, which costs a
  // second COUNT query against the same predicate.
  @Transactional(readOnly = true)
  public Page<CustomerResponse> listByStatus(String status, Pageable pageable) {
    return repository.findByStatus(status, pageable).map(mapper::toResponse);
  }

  // CUS-1001 is public_id, not the surrogate customer_id, so lookups by the id
  // the API speaks go through findByPublicId rather than findById.
  private CustomerEntity load(String publicId) {
    return repository
        .findByPublicId(publicId)
        .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + publicId));
  }

  // Nothing in V1 forces lowercase -- lab 37's ck_customer_email_lower did not
  // survive into the guide's reduced schema -- so this is the only thing
  // stopping Amina@x.com and amina@x.com from both existing.
  private String normalize(String email) {
    return email == null ? null : email.trim().toLowerCase();
  }

  // Validates the normalized email, not the raw one: "  Amina@X.com " is a
  // typing artefact, not an invalid address, and normalize already fixed it.
  private void validate(CreateCustomerRequest request, String email) {
    Map<String, String> fieldErrors = new HashMap<>();
    if (request.name() == null || request.name().isBlank()) {
      fieldErrors.put("name", "Name is required");
    }
    if (email == null || !email.matches("^\\S+@\\S+\\.\\S+$")) {
      fieldErrors.put("email", "Enter a valid email");
    }
    if (request.status() == null || !ALLOWED.containsKey(request.status())) {
      fieldErrors.put("status", "Choose a status");
    }
    if (!fieldErrors.isEmpty()) {
      throw new FieldValidationException(fieldErrors);
    }
  }
}
