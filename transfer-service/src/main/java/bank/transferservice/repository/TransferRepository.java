package bank.transferservice.repository;

import bank.transferservice.entity.Transfer;
import bank.transferservice.entity.TransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


public interface TransferRepository extends JpaRepository<Transfer, UUID> {
    
    List<Transfer> findBySourceIban(String sourceIban);
    
    List<Transfer> findByTargetIban(String targetIban);
    
    List<Transfer> findByStatus(TransferStatus status);
    
    List<Transfer> findBySourceAccountId(UUID sourceAccountId);
    
    List<Transfer> findByTargetAccountId(UUID targetAccountId);
    
    Transfer findByTransactionReference(String transactionReference);
} 