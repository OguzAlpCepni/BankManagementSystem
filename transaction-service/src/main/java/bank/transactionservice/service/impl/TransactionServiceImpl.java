package bank.transactionservice.service.impl;

import bank.transactionservice.client.AccountServiceClient;
import bank.transactionservice.client.TransferServiceClient;
import bank.transactionservice.dto.TransactionRequest;
import bank.transactionservice.dto.TransactionResponse;
import bank.transactionservice.entity.Transaction;
import bank.transactionservice.entity.TransactionStatus;
import bank.transactionservice.entity.TransactionType;
import bank.transactionservice.kafka.TransactionProducer;
import bank.transactionservice.repository.TransactionRepository;
import bank.transactionservice.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;
    private final TransferServiceClient transferServiceClient;
    private final TransactionProducer transactionProducer;

    @Override
    @Transactional
    public TransactionResponse initiateTransaction(TransactionRequest request) {
        log.info("Initiating transaction: {}", request);
        
        // Validate accounts
        ResponseEntity<Boolean> sourceAccountResponse = accountServiceClient.validateAccount(request.getSourceIban());
        if (!Boolean.TRUE.equals(sourceAccountResponse.getBody())) {
            throw new RuntimeException("Source account not found or not valid: " + request.getSourceIban());
        }

        ResponseEntity<Boolean> targetAccountResponse = accountServiceClient.validateAccount(request.getTargetIban());
        if (!Boolean.TRUE.equals(targetAccountResponse.getBody())) {
            throw new RuntimeException("Target account not found or not valid: " + request.getTargetIban());
        }

        // Check sufficient balance
        ResponseEntity<BigDecimal> balanceResponse = accountServiceClient.getAccountBalance(request.getSourceIban());
        BigDecimal balance = balanceResponse.getBody();
        if (balance == null || balance.compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance in source account");
        }

        // Create and save transaction
        Transaction transaction = Transaction.builder()
                .sourceAccountId(request.getSourceAccountId())
                .targetAccountId(request.getTargetAccountId())
                .sourceIban(request.getSourceIban())
                .targetIban(request.getTargetIban())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .description(request.getDescription())
                .type(request.getType())
                .status(TransactionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .sourceAccountDebited(false)
                .targetAccountCredited(false)
                .build();

        transaction = transactionRepository.save(transaction);
        
        // Publish transaction created event
        transactionProducer.sendTransactionCreatedEvent(transaction);
        
        // Start transaction processing
        debitSourceAccount(transaction);
        
        return mapToTransactionResponse(transaction);
    }

    @Override
    @Transactional
    public void debitSourceAccount(Transaction transaction) {
        log.info("Debiting source account for transaction: {}", transaction.getId());
        
        try {
            AccountServiceClient.DebitRequest debitRequest = new AccountServiceClient.DebitRequest(
                    transaction.getAmount(),
                    transaction.getCurrency(),
                    transaction.getId(),
                    transaction.getDescription()
            );
            
            ResponseEntity<Boolean> response = accountServiceClient.debitAccount(
                    transaction.getSourceIban(),
                    debitRequest
            );
            
            if (!Boolean.TRUE.equals(response.getBody())) {
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setFailedAt(LocalDateTime.now());
                transaction.setErrorMessage("Failed to debit source account");
                transactionRepository.save(transaction);
                transactionProducer.sendTransactionFailedEvent(transaction);
            }
            
            // The account-service will publish ACCOUNT_DEBITED event that will be handled by
            // AccountEventConsumer, which will update the transaction status
        } catch (Exception e) {
            log.error("Error while debiting source account", e);
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailedAt(LocalDateTime.now());
            transaction.setErrorMessage("Error while debiting source account: " + e.getMessage());
            transactionRepository.save(transaction);
            transactionProducer.sendTransactionFailedEvent(transaction);
        }
    }

    @Override
    @Transactional
    public void creditTargetAccount(Transaction transaction) {
        log.info("Crediting target account for transaction: {}", transaction.getId());
        
        try {
            if (transaction.getType() == TransactionType.INTERNAL_TRANSFER) {
                // Direct credit for internal transfers
                AccountServiceClient.CreditRequest creditRequest = new AccountServiceClient.CreditRequest(
                        transaction.getAmount(),
                        transaction.getCurrency(),
                        transaction.getId(),
                        transaction.getDescription()
                );
                
                ResponseEntity<Boolean> response = accountServiceClient.creditAccount(
                        transaction.getTargetIban(),
                        creditRequest
                );
                
                if (!Boolean.TRUE.equals(response.getBody())) {
                    transaction.setStatus(TransactionStatus.FAILED);
                    transaction.setFailedAt(LocalDateTime.now());
                    transaction.setErrorMessage("Failed to credit target account");
                    transactionRepository.save(transaction);
                    transactionProducer.sendTransactionFailedEvent(transaction);
                    // Initiate compensation
                    compensateFailedTransaction(transaction);
                }
                
                // The account-service will publish ACCOUNT_CREDITED event that will be handled by
                // AccountEventConsumer, which will update the transaction status
            } else {
                // For external transfers (EFT, SWIFT, FAST), create a transfer request
                TransferServiceClient.TransferRequest transferRequest = new TransferServiceClient.TransferRequest(
                        transaction.getSourceAccountId(),
                        transaction.getTargetAccountId(),
                        transaction.getSourceIban(),
                        transaction.getTargetIban(),
                        transaction.getAmount(),
                        transaction.getCurrency(),
                        transaction.getDescription(),
                        transaction.getType(),
                        transaction.getId()
                );
                
                transferServiceClient.initiateTransfer(transferRequest);
                
                // TransferEventConsumer will handle the response from transfer service
            }
        } catch (Exception e) {
            log.error("Error while crediting target account", e);
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailedAt(LocalDateTime.now());
            transaction.setErrorMessage("Error while crediting target account: " + e.getMessage());
            transactionRepository.save(transaction);
            transactionProducer.sendTransactionFailedEvent(transaction);
            // Initiate compensation if source account was debited
            if (transaction.isSourceAccountDebited()) {
                compensateFailedTransaction(transaction);
            }
        }
    }

    @Override
    @Transactional
    public void compensateFailedTransaction(Transaction transaction) {
        log.info("Initiating compensation for failed transaction: {}", transaction.getId());

        if (!transaction.isSourceAccountDebited()) {
            log.info("No compensation needed, source account was not debited");
            return;
        }
        
        try {
            // Create a compensating (reversal) transaction
            Transaction compensationTransaction = Transaction.builder()
                    .sourceAccountId(transaction.getTargetAccountId())
                    .targetAccountId(transaction.getSourceAccountId())
                    .sourceIban(transaction.getTargetIban())
                    .targetIban(transaction.getSourceIban())
                    .amount(transaction.getAmount())
                    .currency(transaction.getCurrency())
                    .description("Compensation for failed transaction: " + transaction.getId())
                    .type(TransactionType.COMPENSATION)
                    .status(TransactionStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .sourceAccountDebited(false)
                    .targetAccountCredited(false)
                    .build();
            
            compensationTransaction = transactionRepository.save(compensationTransaction);
            
            // Credit back the source account
            AccountServiceClient.CreditRequest creditRequest = new AccountServiceClient.CreditRequest(
                    transaction.getAmount(),
                    transaction.getCurrency(),
                    compensationTransaction.getId(),
                    compensationTransaction.getDescription()
            );

            accountServiceClient.creditAccount(
                    transaction.getSourceIban(),
                    creditRequest
            );
            
            log.info("Compensation transaction created: {}", compensationTransaction.getId());
        } catch (Exception e) {
            log.error("Error while compensating failed transaction", e);
        }
    }

    @Override
    public TransactionResponse getTransactionById(UUID transactionId) {
        Transaction transaction = findTransactionById(transactionId);
        if (transaction == null) {
            throw new RuntimeException("Transaction not found with ID: " + transactionId);
        }
        return mapToTransactionResponse(transaction);
    }

    @Override
    public List<TransactionResponse> getTransactionsBySourceAccountId(UUID sourceAccountId) {
        List<Transaction> transactions = transactionRepository.findBySourceAccountId(sourceAccountId);
        return transactions.stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> getTransactionsByTargetAccountId(UUID targetAccountId) {
        List<Transaction> transactions = transactionRepository.findByTargetAccountId(targetAccountId);
        return transactions.stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> getTransactionsBySourceIban(String sourceIban) {
        List<Transaction> transactions = transactionRepository.findBySourceIban(sourceIban);
        return transactions.stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> getTransactionsByTargetIban(String targetIban) {
        List<Transaction> transactions = transactionRepository.findByTargetIban(targetIban);
        return transactions.stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> getTransactionsByStatus(TransactionStatus status) {
        List<Transaction> transactions = transactionRepository.findByStatus(status);
        return transactions.stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TransactionResponse cancelTransaction(UUID transactionId) {
        Transaction transaction = findTransactionById(transactionId);
        if (transaction == null) {
            throw new RuntimeException("Transaction not found with ID: " + transactionId);
        }
        
        if (transaction.getStatus() == TransactionStatus.COMPLETED) {
            throw new RuntimeException("Cannot cancel a completed transaction");
        }
        
        if (transaction.getStatus() == TransactionStatus.CANCELLED) {
            return mapToTransactionResponse(transaction);
        }
        
        transaction.setStatus(TransactionStatus.CANCELLED);
        transaction = transactionRepository.save(transaction);
        
        // If the source account was already debited, initiate a compensation transaction
        if (transaction.isSourceAccountDebited()) {
            compensateFailedTransaction(transaction);
        }
        
        // If it's an external transfer, also cancel the transfer
        if (transaction.getType() != TransactionType.INTERNAL_TRANSFER &&
                transaction.getType() != TransactionType.COMPENSATION) {
            try {
                transferServiceClient.cancelTransfer(transactionId);
            } catch (Exception e) {
                log.error("Error cancelling external transfer", e);
            }
        }
        
        return mapToTransactionResponse(transaction);
    }

    @Override
    @Transactional
    public TransactionResponse updateTransactionStatus(UUID transactionId, TransactionStatus status) {
        Transaction transaction = findTransactionById(transactionId);
        if (transaction == null) {
            throw new RuntimeException("Transaction not found with ID: " + transactionId);
        }
        
        transaction.setStatus(status);
        
        if (status == TransactionStatus.COMPLETED) {
            transaction.setCompletedAt(LocalDateTime.now());
            transactionProducer.sendTransactionCompletedEvent(transaction);
        } else if (status == TransactionStatus.FAILED) {
            transaction.setFailedAt(LocalDateTime.now());
            transactionProducer.sendTransactionFailedEvent(transaction);
        }
        
        transaction = transactionRepository.save(transaction);
        return mapToTransactionResponse(transaction);
    }

    @Override
    public Transaction findTransactionById(UUID transactionId) {
        return transactionRepository.findById(transactionId).orElse(null);
    }

    @Override
    @Transactional
    public Transaction updateTransaction(Transaction transaction) {
        transaction = transactionRepository.save(transaction);
        
        if (transaction.getStatus() == TransactionStatus.COMPLETED) {
            transactionProducer.sendTransactionCompletedEvent(transaction);
        } else if (transaction.getStatus() == TransactionStatus.FAILED) {
            transactionProducer.sendTransactionFailedEvent(transaction);
        }
        
        return transaction;
    }

    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .sourceAccountId(transaction.getSourceAccountId())
                .targetAccountId(transaction.getTargetAccountId())
                .sourceIban(transaction.getSourceIban())
                .targetIban(transaction.getTargetIban())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .description(transaction.getDescription())
                .type(transaction.getType())
                .status(transaction.getStatus())
                .createdAt(transaction.getCreatedAt())
                .completedAt(transaction.getCompletedAt())
                .failedAt(transaction.getFailedAt())
                .sourceAccountDebited(transaction.isSourceAccountDebited())
                .targetAccountCredited(transaction.isTargetAccountCredited())
                .errorMessage(transaction.getErrorMessage())
                .build();
    }
} 