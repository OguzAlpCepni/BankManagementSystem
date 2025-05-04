package bank.transactionservice.repository;

import bank.transactionservice.entity.Transaction;
import bank.transactionservice.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    
    List<Transaction> findBySourceAccountId(UUID sourceAccountId);
    
    List<Transaction> findByTargetAccountId(UUID targetAccountId);
    
    List<Transaction> findBySourceIban(String sourceIban);
    
    List<Transaction> findByTargetIban(String targetIban);
    
    List<Transaction> findByStatus(TransactionStatus status);
    
    Optional<Transaction> findByIdAndStatus(UUID id, TransactionStatus status);
    
    List<Transaction> findBySourceAccountIdAndStatus(UUID sourceAccountId, TransactionStatus status);
    
    List<Transaction> findByTargetAccountIdAndStatus(UUID targetAccountId, TransactionStatus status);
} 