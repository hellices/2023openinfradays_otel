package com.otel.server.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.otel.server.domain.Company;

public interface CompanyRepository extends JpaRepository<Company, String> {
    // find one by property
    Optional<Company> findOneByNameContainsIgnoreCase(String name);
}
