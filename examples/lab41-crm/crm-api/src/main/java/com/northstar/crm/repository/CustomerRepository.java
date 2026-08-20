package com.northstar.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {
    Optional<CustomerEntity> findByPublicId(String publicId);
    boolean existsByEmail(String email);
    Page<CustomerEntity> findByStatus(String status, Pageable pageable);
}