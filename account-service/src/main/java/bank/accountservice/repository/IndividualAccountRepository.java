package bank.accountservice.repository;

import bank.accountservice.entity.IndividualAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface IndividualAccountRepository extends JpaRepository<IndividualAccount, UUID> {
    Optional<IndividualAccount> findByIban(String iban);
    Optional<IndividualAccount> findByIdentityNumber(String identityNumber);
    List<IndividualAccount> findByCustomerId(UUID customerId);
    long countByCustomerId(UUID customerId);
}
