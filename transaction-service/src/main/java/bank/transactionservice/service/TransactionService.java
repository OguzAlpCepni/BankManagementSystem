package bank.transactionservice.service;

import bank.transactionservice.dto.TransactionRequest;
import bank.transactionservice.dto.TransactionResponse;
import bank.transactionservice.entity.Transaction;
import bank.transactionservice.entity.TransactionStatus;

import java.util.List;
import java.util.UUID;

public interface TransactionService {
    
    TransactionResponse initiateTransaction(TransactionRequest request);
    
    TransactionResponse getTransactionById(UUID transactionId);
    
    List<TransactionResponse> getTransactionsBySourceAccountId(UUID sourceAccountId);
    
    List<TransactionResponse> getTransactionsByTargetAccountId(UUID targetAccountId);
    
    List<TransactionResponse> getTransactionsBySourceIban(String sourceIban);
    
    List<TransactionResponse> getTransactionsByTargetIban(String targetIban);
    
    List<TransactionResponse> getTransactionsByStatus(TransactionStatus status);
    
    TransactionResponse cancelTransaction(UUID transactionId);
    
    TransactionResponse updateTransactionStatus(UUID transactionId, TransactionStatus status);
    
    void debitSourceAccount(Transaction transaction);
    
    void creditTargetAccount(Transaction transaction);
    
    void compensateFailedTransaction(Transaction transaction);
    
    Transaction findTransactionById(UUID transactionId);
    
    Transaction updateTransaction(Transaction transaction);
} 