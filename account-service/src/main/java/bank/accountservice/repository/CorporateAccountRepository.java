package bank.accountservice.repository;

import bank.accountservice.entity.CorporateAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CorporateAccountRepository extends JpaRepository<CorporateAccount, UUID> {
}
