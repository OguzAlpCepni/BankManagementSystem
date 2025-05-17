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
    
    @Override
    public void sendTransactionCompletedEvent(Transaction transaction) {
        TransactionEvent event = buildTransactionEvent(transaction, "COMPLETED");
        log.info("Sending transaction completed event: {}", event);
        boolean result = streamBridge.send("transactionCompleted-out-0", event);
        log.info("Event sent: {}", result);
    }
    
    @Override
    public void sendTransactionFailedEvent(Transaction transaction) {
        TransactionEvent event = buildTransactionEvent(transaction, "FAILED");
        log.info("Sending transaction failed event: {}", event);
        boolean result = streamBridge.send("transactionCompleted-out-0", event);
        log.info("Event sent: {}", result);
    }
    
    private TransactionEvent buildTransactionEvent(Transaction transaction, String eventType) {
        return TransactionEvent.builder()
                .transactionId(transaction.getId())
                .transferId(transaction.getTransferId())
                .sourceAccountId(transaction.getSourceAccountId())
                .targetAccountId(transaction.getTargetAccountId())
                .sourceIban(transaction.getSourceIban())
                .targetIban(transaction.getTargetIban())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .description(transaction.getId().toString()) // Using ID as description for transfer lookup
                .type(transaction.getType().name())
                .status(transaction.getStatus().name())
                .timestamp(LocalDateTime.now())
                .eventType(eventType)
                .build();
    }
} 