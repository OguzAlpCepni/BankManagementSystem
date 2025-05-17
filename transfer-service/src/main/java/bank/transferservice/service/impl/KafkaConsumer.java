package bank.transferservice.service.impl;


import bank.transferservice.service.TransferProcessorService;
import io.github.oguzalpcepni.event.TransactionEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {

    private final TransferProcessorService transferProcessorService;

    @Bean
    @Transactional
    public Consumer<TransactionEvent> transactionEventConsumer() {
        return event -> {
            log.info("Received transaction event: {}", event);

            try {
                transferProcessorService.processTransferResponse(event);
            } catch (Exception e) {
                log.error("Error processing transaction event: {}", e.getMessage(), e);
            }
        };
    }
} 