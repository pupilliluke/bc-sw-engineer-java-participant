package com.northstar.crm.repository;

import java.time.Instant;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Step 9's data loader. Flyway owns the schema and deliberately does not carry
 * the fixtures: a migration that inserts Amina and Ravi would put lab data into
 * every environment that ever runs it. Seeding here keeps the two concerns
 * apart -- structure is versioned, sample rows are not.
 *
 * Idempotent by public_id, so restarting the app does not duplicate anyone and
 * does not overwrite edits made through the API.
 */
@Component
public class FixtureLoader implements CommandLineRunner {

  private final CustomerRepository repository;

  public FixtureLoader(CustomerRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional
  public void run(String... args) {
    seed("CUS-1001", "Amina Khan", "amina@example.com", "ACTIVE");
    seed("CUS-1002", "Ravi Singh", "ravi@example.com", "PROSPECT");
  }

  private void seed(String publicId, String fullName, String email, String status) {
    if (repository.findByPublicId(publicId).isPresent()) {
      return;
    }
    CustomerEntity entity = new CustomerEntity();
    entity.setPublicId(publicId);
    entity.setFullName(fullName);
    entity.setEmail(email);
    entity.setStatus(status);
    entity.setCreatedAt(Instant.now());
    repository.save(entity);
  }
}
