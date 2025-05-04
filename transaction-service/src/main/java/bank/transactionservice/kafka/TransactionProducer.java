package bank.transactionservice.kafka;

import bank.transactionservice.entity.Transaction;
import io.github.oguzalpcepni.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionProducer implements KafkaProducerService {

    private final StreamBridge streamBridge;
    
    public void sendTransactionCreatedEvent(Transaction transaction) {
        TransactionEvent event = buildTransactionEvent(transaction, "CREATED");
        log.info("Sending transaction created event: {}", event);
        boolean result = streamBridge.send("transactionCreated-out-0",event);
        log.info("Event sent: {}", result);
        log.info("Transfer event sent successfully");
    }
    
    public void sendTransactionCompletedEvent(Transaction transaction) {
        TransactionEvent event = buildTransactionEvent(transaction, "COMPLETED");
        log.info("Sending transaction completed event: {}", event);
        boolean result = streamBridge.send("transactionCompleted-out-0",event);
        log.info("Event sent: {}", result);
        log.info("Transfer event sent successfully");
    }
    
    public void sendTransactionFailedEvent(Transaction transaction) {
        TransactionEvent event = buildTransactionEvent(transaction, "FAILED");
        log.info("Sending transaction failed event: {}", event);
        boolean result = streamBridge.send("transactionCompleted-out-0",event);
        log.info("Event sent: {}", result);
        log.info("Transfer event sent successfully");
    }
    
    private TransactionEvent buildTransactionEvent(Transaction transaction, String eventType) {
        return TransactionEvent.builder()
                .transactionId(transaction.getId())
                .sourceAccountId(transaction.getSourceAccountId())
                .targetAccountId(transaction.getTargetAccountId())
                .sourceIban(transaction.getSourceIban())
                .targetIban(transaction.getTargetIban())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .description(transaction.getDescription())
                .type(transaction.getType().toString())
                .status(transaction.getStatus().toString())
                .timestamp(LocalDateTime.now())
                .eventType(eventType)
                .build();
    }
} 