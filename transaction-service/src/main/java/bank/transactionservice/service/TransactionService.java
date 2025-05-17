package bank.transactionservice.service;

import bank.transactionservice.entity.Transaction;
import bank.transactionservice.entity.TransactionStatus;
import io.github.oguzalpcepni.event.TransferEvent;

import java.util.List;
import java.util.UUID;

public interface TransactionService {
    Transaction createTransaction(TransferEvent transferEvent);
    List<Transaction> findBySourceAccountId(UUID sourceAccountId);
    List<Transaction> findByTargetAccountId(UUID targetAccountId);
    List<Transaction> findByStatus(TransactionStatus status);
}
    
