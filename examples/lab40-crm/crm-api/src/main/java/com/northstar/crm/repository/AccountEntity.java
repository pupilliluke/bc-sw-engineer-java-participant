package com.northstar.crm.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "account")
public class AccountEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "account_id")
  private Long id;

  // Template used @Column here; an association is mapped with @JoinColumn.
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "customer_id", nullable = false)
  private CustomerEntity customer;

  @Column(name = "balance", nullable = false, precision = 19, scale = 2)
  private BigDecimal balance;

  // Not in the step 6 template; V1 declares status NOT NULL.
  @Column(name = "status", nullable = false)
  private String status;

  // Step 7. id is the only key an account has, so equality waits for the
  // insert. customer is excluded from toString: it is a lazy proxy and
  // printing it would load the row.
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AccountEntity other)) {
      return false;
    }
    return id != null && id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return AccountEntity.class.hashCode();
  }

  @Override
  public String toString() {
    return "AccountEntity{id=" + id + ", status=" + status + "}";
  }
}
