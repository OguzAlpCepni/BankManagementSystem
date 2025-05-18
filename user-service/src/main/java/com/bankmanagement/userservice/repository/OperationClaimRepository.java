package com.bankmanagement.userservice.repository;

import com.bankmanagement.userservice.entity.OperationClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OperationClaimRepository extends JpaRepository<OperationClaim, UUID> {
    Optional<OperationClaim> findByName(String name);
} 