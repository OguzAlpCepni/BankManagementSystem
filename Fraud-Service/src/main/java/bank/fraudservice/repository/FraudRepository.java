package bank.fraudservice.repository;

import bank.fraudservice.entity.Fraud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FraudRepository extends JpaRepository<Fraud, UUID> {
}
