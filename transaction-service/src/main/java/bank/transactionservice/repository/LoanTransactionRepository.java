package bank.transactionservice.repository;

import bank.transactionservice.entity.LoanTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LoanTransactionRepository extends JpaRepository<LoanTransaction, UUID> {
    Optional<LoanTransaction> findByLoanId(UUID loanId);
}
