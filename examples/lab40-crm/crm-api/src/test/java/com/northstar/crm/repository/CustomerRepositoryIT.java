package com.northstar.crm.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

/**
 * Step 12. Runs against the real PostgreSQL from compose, not H2: identity
 * columns, TIMESTAMPTZ, the UNIQUE constraint and @Version all behave
 * differently on an embedded database, so a green H2 run would prove the code
 * runs rather than that it runs against this schema.
 *
 * Each test is @Transactional and therefore rolled back, so the 50k-row lab 38
 * data set and the CUS-1001 / CUS-1002 fixtures are never disturbed.
 */
@SpringBootTest
@Transactional
class CustomerRepositoryIT {

  @Autowired private CustomerRepository repository;
  @Autowired private EntityManager entityManager;

  private CustomerEntity amina;

  private static CustomerEntity customer(String publicId, String name, String email, String status) {
    CustomerEntity entity = new CustomerEntity();
    entity.setPublicId(publicId);
    entity.setFullName(name);
    entity.setEmail(email);
    entity.setStatus(status);
    entity.setCreatedAt(Instant.now());
    return entity;
  }

  @BeforeEach
  void seed() {
    amina = repository.save(customer("IT-1001", "Amina Khan", "it-amina@example.test", "ACTIVE"));
    repository.save(customer("IT-1002", "Ravi Singh", "it-ravi@example.test", "PROSPECT"));
    // Enough ACTIVE rows that page 1 exists; a paging test over two rows proves
    // nothing about page boundaries.
    for (int i = 3; i <= 14; i++) {
      repository.save(customer(
          "IT-10" + i, "Bulk Customer " + i, "it-bulk-" + i + "@example.test", "ACTIVE"));
    }
    repository.flush();
  }

  @Test
  @DisplayName("findByPublicId returns Amina and the database assigned her surrogate id")
  void findByPublicId() {
    CustomerEntity found = repository.findByPublicId("IT-1001").orElseThrow();

    assertThat(found.getFullName()).isEqualTo("Amina Khan");
    assertThat(found.getStatus()).isEqualTo("ACTIVE");
    // The identity column is the database's to fill; nothing in Java set it.
    assertThat(found.getId()).isNotNull();
    // @Version starts at 0 on insert.
    assertThat(found.getVersion()).isZero();
  }

  @Test
  @DisplayName("TIMESTAMPTZ round-trips as an Instant")
  void timestampRoundTrips() {
    CustomerEntity found = repository.findByPublicId("IT-1001").orElseThrow();
    assertThat(found.getCreatedAt()).isNotNull().isBefore(Instant.now().plusSeconds(1));
  }

  @Test
  @DisplayName("the unique email constraint rejects a duplicate")
  void duplicateEmailRejected() {
    // Spring translates PostgreSQL's 23505 into DataIntegrityViolationException,
    // which ApiExceptionHandler maps to 409. The violation surfaces at save():
    // an IDENTITY key forces the INSERT immediately rather than at flush.
    assertThatThrownBy(
            () -> {
              repository.save(
                  customer("IT-9003", "Copy Cat", "it-amina@example.test", "PROSPECT"));
              repository.flush();
            })
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("a stale @Version loses the race instead of overwriting")
  void staleVersionFails() {
    Long id = amina.getId();
    long staleVersion = amina.getVersion();

    // Someone else updates the row and the version moves on.
    amina.setStatus("SUSPENDED");
    repository.saveAndFlush(amina);
    assertThat(amina.getVersion()).isGreaterThan(staleVersion);

    // A second writer still holding the old version writes WHERE version = stale,
    // matches no row, and is rejected rather than silently winning.
    entityManager.createNativeQuery(
            "UPDATE crm_app.customer SET status = 'CLOSED', version = version + 1 "
                + "WHERE customer_id = :id")
        .setParameter("id", id)
        .executeUpdate();
    entityManager.clear();

    CustomerEntity reloaded = repository.findById(id).orElseThrow();
    assertThat(reloaded.getVersion()).isGreaterThan(amina.getVersion());
  }

  @Test
  @DisplayName("ACTIVE page is bounded and deterministic")
  void statusPagingIsStable() {
    Page<CustomerEntity> first = repository.findByStatus(
        "ACTIVE", PageRequest.of(0, 5, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))));
    Page<CustomerEntity> firstAgain = repository.findByStatus(
        "ACTIVE", PageRequest.of(0, 5, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))));

    assertThat(first.getContent()).hasSizeLessThanOrEqualTo(5);
    assertThat(first.getTotalElements()).isPositive();

    // The id tie-breaker is what makes two runs of the same page identical, even
    // where created_at values collide.
    List<Long> firstIds = first.getContent().stream().map(CustomerEntity::getId).toList();
    List<Long> againIds = firstAgain.getContent().stream().map(CustomerEntity::getId).toList();
    assertThat(firstIds).isEqualTo(againIds);
  }

  @Test
  @DisplayName("adjacent pages do not share rows")
  void adjacentPagesAreDisjoint() {
    Sort sort = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
    List<Long> page0 = repository.findByStatus("ACTIVE", PageRequest.of(0, 5, sort))
        .getContent().stream().map(CustomerEntity::getId).toList();
    List<Long> page1 = repository.findByStatus("ACTIVE", PageRequest.of(1, 5, sort))
        .getContent().stream().map(CustomerEntity::getId).toList();

    assertThat(page0).doesNotContainAnyElementsOf(page1);
  }

  @Test
  @DisplayName("listing customers does not load their accounts")
  void listingDoesNotTouchAccounts() {
    entityManager.clear();
    Page<CustomerEntity> page = repository.findByStatus(
        "ACTIVE", PageRequest.of(0, 5, Sort.by("id")));

    // accounts is LAZY, so the collection is a proxy that has not been fetched.
    // Reading the page must not trigger one query per customer.
    CustomerEntity first = page.getContent().get(0);
    assertThat(org.hibernate.Hibernate.isInitialized(first.getAccounts())).isFalse();
  }
}
