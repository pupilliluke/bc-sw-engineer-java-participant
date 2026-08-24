package com.northstar.crm.service;

import com.northstar.crm.api.CreateCustomerRequest;
import com.northstar.crm.api.CustomerResponse;
import com.northstar.crm.repository.CustomerEntity;
import java.time.Instant;
import org.springframework.stereotype.Component;

/**
 * The boundary between the API shape and the row shape. id maps to public_id
 * and name maps to full_name, the pairing the UI and the API have used since
 * lab 34; the surrogate customer_id and the @Version counter stop here.
 */
@Component
public class CustomerMapper {

  /**
   * The caller passes the already-normalized email so the value written to the
   * row is the same one existsByEmail was checked against.
   *
   * created_at is set here rather than left to the column default: V1 declares
   * it NOT NULL DEFAULT CURRENT_TIMESTAMP, but Hibernate names every mapped
   * column in the INSERT, so an unset field sends NULL and the default never
   * applies.
   */
  public CustomerEntity toEntity(CreateCustomerRequest request, String email) {
    CustomerEntity entity = new CustomerEntity();
    entity.setPublicId(request.publicId());
    entity.setFullName(request.name());
    entity.setEmail(email);
    entity.setStatus(request.status());
    entity.setCreatedAt(Instant.now());
    return entity;
  }

  public CustomerResponse toResponse(CustomerEntity entity) {
    return new CustomerResponse(
        entity.getPublicId(),
        entity.getFullName(),
        entity.getEmail(),
        entity.getStatus());
  }
}
