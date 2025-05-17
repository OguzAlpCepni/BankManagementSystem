package bank.transactionservice.kafka;

import bank.transactionservice.service.TransactionService;
import io.github.oguzalpcepni.event.TransferEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * Transfer servisinden gelen olayları işleyen Kafka consumer sınıfı.
 * Saga pattern'de bir katılımcı olarak görev yapar.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TransferEventConsumer {
    
    private final TransactionService transactionService;
    
    @Bean
    public Consumer<TransferEvent> handleTransferDebitedEvent() {
        return transferEvent -> {
            if ("DEBITED".equals(transferEvent.getType())) {
                log.info("Received transfer DEBITED event: {}", transferEvent);
                try {
                    // Create a new transaction from the transfer event
                    transactionService.createTransaction(transferEvent);
                } catch (Exception e) {
                    log.error("Error processing transfer debited event: {}", e.getMessage(), e);
                }
            }
        };
    }
} 