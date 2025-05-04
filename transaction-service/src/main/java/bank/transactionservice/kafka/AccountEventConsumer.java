package bank.transactionservice.kafka;

import bank.transactionservice.entity.Transaction;
import bank.transactionservice.entity.TransactionStatus;
import bank.transactionservice.entity.TransactionType;

import bank.transactionservice.service.TransactionService;
import io.github.oguzalpcepni.event.AccountEvent;
import io.github.oguzalpcepni.event.TransferEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountEventConsumer {
    
    private final TransactionService transactionService;
    
    @Bean
    public Consumer<AccountEvent> handleAccountDebitedEvent() {

        return event -> {
            log.info("Received account debited event: {}", event);
            UUID transactionId = event.getTransactionId();
            Transaction transaction = transactionService.findTransactionById(transactionId);

            if (transaction == null) {
                log.error("Transaction not found for ID: {}", transactionId);

            }
            // Update transaction to reflect source account was debited
            transaction.setSourceAccountDebited(true);
            transaction.setStatus(TransactionStatus.SOURCE_ACCOUNT_DEBITED);

            transactionService.updateTransaction(transaction);
            log.info("Transaction updated - source account debited: {}", transaction.getId());

            // For internal transfers, proceed with crediting the target account
            if (transaction.getType() == TransactionType.INTERNAL_TRANSFER) {
                transactionService.creditTargetAccount(transaction);
            }
            // Update transaction to reflect source account was debited
            transaction.setSourceAccountDebited(true);
            transaction.setStatus(TransactionStatus.SOURCE_ACCOUNT_DEBITED);

            transactionService.updateTransaction(transaction);
            log.info("Transaction updated - source account debited: {}", transaction.getId());

            // For internal transfers, proceed with crediting the target account
            if (transaction.getType() == TransactionType.INTERNAL_TRANSFER) {
                transactionService.creditTargetAccount(transaction);
            }
        };

    }
    
    @Bean
    public Consumer<AccountEvent> handleAccountCreditedEvent() {
        return event -> {
            log.info("Received account credited event: {}", event);

            UUID transactionId = event.getTransactionId();
            Transaction transaction = transactionService.findTransactionById(transactionId);

            if (transaction == null) {
                log.error("Transaction not found for ID: {}", transactionId);
            }

            // Update transaction to reflect target account was credited
            transaction.setTargetAccountCredited(true);
            transaction.setStatus(TransactionStatus.TARGET_ACCOUNT_CREDITED);

            // For internal transfers, complete the transaction
            if (transaction.getType() == TransactionType.INTERNAL_TRANSFER) {
                transaction.setStatus(TransactionStatus.COMPLETED);
                transaction.setCompletedAt(LocalDateTime.now());
            }

            transactionService.updateTransaction(transaction);
            log.info("Transaction updated - target account credited: {}", transaction.getId());
        };

    }
    
    @Bean
    public Consumer<AccountEvent> handleAccountDebitFailedEvent() {
        return event -> {
            log.info("Received account debit failed event: {}", event);

            UUID transactionId = event.getTransactionId();
            Transaction transaction = transactionService.findTransactionById(transactionId);

            if (transaction == null) {
                log.error("Transaction not found for ID: {}", transactionId);
                return;
            }

            // Update transaction status to FAILED
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailedAt(LocalDateTime.now());
            transaction.setErrorMessage("Account debit failed: " + event.getErrorMessage());

            transactionService.updateTransaction(transaction);
            log.info("Transaction failed - source account debit failed: {}", transaction.getId());

        };
    }
    
    @Bean
    public Consumer<AccountEvent> handleAccountCreditFailedEvent() {
        return event -> {
            log.info("Received account credit failed event: {}", event);

            UUID transactionId = event.getTransactionId();
            Transaction transaction = transactionService.findTransactionById(transactionId);

            if (transaction == null) {
                log.error("Transaction not found for ID: {}", transactionId);
                return;
            }

            // Update transaction status to FAILED
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailedAt(LocalDateTime.now());
            transaction.setErrorMessage("Account credit failed: " + event.getErrorMessage());

            // Initiate compensating transaction if source was already debited
            if (transaction.isSourceAccountDebited()) {
                transactionService.compensateFailedTransaction(transaction);
            }

            transactionService.updateTransaction(transaction);
            log.info("Transaction failed - target account credit failed: {}", transaction.getId());

        };
    }
} 