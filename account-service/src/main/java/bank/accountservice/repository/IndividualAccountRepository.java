package bank.accountservice.repository;

import bank.accountservice.entity.IndividualAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IndividualAccountRepository extends JpaRepository<IndividualAccount, UUID> {
}
