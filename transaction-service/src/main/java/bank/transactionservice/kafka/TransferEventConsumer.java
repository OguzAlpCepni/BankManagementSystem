package bank.transactionservice.kafka;

import bank.transactionservice.entity.Transaction;
import bank.transactionservice.entity.TransactionStatus;
import io.github.oguzalpcepni.event.TransferEvent;
import bank.transactionservice.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransferEventConsumer {
    
    private final TransactionService transactionService;
    
    @Bean
    public Consumer<TransferEvent> handleTransferCompletedEvent() {

        return event -> {
            log.info("Received transfer completed event: {}", event);
            UUID transactionId = UUID.fromString(event.getTransactionReference());
            Transaction transaction = transactionService.findTransactionById(transactionId);
            if (transaction == null) {
                log.error("Transaction not found for ID: {}", transactionId);
                return;
            }
            // Update transaction status to COMPLETED
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setCompletedAt(LocalDateTime.now());
            transaction.setTargetAccountCredited(event.getTargetAccountCredited());

            transactionService.updateTransaction(transaction);
            log.info("Transaction completed: {}", transaction.getId());
        };
    }

    @Bean
    public Consumer<TransferEvent> handleTransferFailedEvent() {
        return event -> {
            log.info("Received transfer failed event: {}", event);
            UUID transactionId = UUID.fromString(event.getTransactionReference());
            Transaction transaction = transactionService.findTransactionById(transactionId);

            if (transaction == null) {
                log.error("Transaction not found for ID: {}", transactionId);
                return;
            }

            // Update transaction status to FAILED
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailedAt(LocalDateTime.now());
            transaction.setErrorMessage(event.getDescription()); // Using description field for error message

            transactionService.updateTransaction(transaction);
            log.info("Transaction failed: {}", transaction.getId());
        };
    }
} 