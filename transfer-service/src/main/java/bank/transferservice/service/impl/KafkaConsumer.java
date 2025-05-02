package bank.transferservice.service.impl;

import bank.transferservice.dto.event.TransferEvent;
import bank.transferservice.service.TransferProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {

    private final TransferProcessorService transferProcessorService;

    @Bean
    public Consumer<TransferEvent> transferResponseConsumer() {
        return event -> {
            log.info("Received transfer response event: {}", event);
            transferProcessorService.processTransferResponse(event);
        };
    }
} 