package com.northstar.crm.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "customer")
public class CustomerEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "customer_id")
  private Long id;

  @Column(name = "public_id", nullable = false, unique = true)
  private String publicId;

  // Not in the step 5 template, but V1 declares full_name NOT NULL, so an
  // insert without it fails.
  @Column(name = "full_name", nullable = false)
  private String fullName;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  // Template had @Enumerated(EnumType.STRING) here; that annotation is only
  // valid on an enum field and throws at startup against a String.
  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Version
  @Column(name = "version")
  private long version;

  @OneToMany(mappedBy = "customer")
  private Set<AccountEntity> accounts = new HashSet<>();


  // Accessors exist because CustomerMapper calls them. Hibernate itself uses
  // field access here -- @Id is on the field -- so JPA needs none of these.
  public String getPublicId() {
    return publicId;
  }

  public void setPublicId(String publicId) {
    this.publicId = publicId;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public void setCreatedAt(java.time.Instant createdAt) {
    this.createdAt = createdAt;
  }


  public Long getId() {
    return id;
  }

  public java.time.Instant getCreatedAt() {
    return createdAt;
  }

  public long getVersion() {
    return version;
  }

  public java.util.Set<AccountEntity> getAccounts() {
    return accounts;
  }

  // Step 7. publicId is the business key: id is null until the row is inserted,
  // so it cannot carry identity, and accounts is lazy, so touching it here
  // would trigger a load or a LazyInitializationException with
  // open-in-view false.
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CustomerEntity other)) {
      return false;
    }
    return publicId != null && publicId.equals(other.publicId);
  }

  // Constant so the hash does not change when the entity moves from transient
  // to persistent, which would lose it inside any HashSet it already sits in.
  @Override
  public int hashCode() {
    return CustomerEntity.class.hashCode();
  }

  // accounts excluded: printing a lazy collection loads it.
  @Override
  public String toString() {
    return "CustomerEntity{publicId=" + publicId + ", status=" + status + "}";
  }
}
