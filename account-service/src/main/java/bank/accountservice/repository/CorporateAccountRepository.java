package bank.accountservice.repository;

import bank.accountservice.entity.CorporateAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface CorporateAccountRepository extends JpaRepository<CorporateAccount, UUID> {
    Optional<CorporateAccount> findByIban(String iban);
    Optional<CorporateAccount> findByTaxNumber(String taxNumber);
    List<CorporateAccount> findByCustomerId(UUID customerId);
    long countByCustomerId(UUID customerId);
}
