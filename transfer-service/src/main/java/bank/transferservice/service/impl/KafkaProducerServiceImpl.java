package bank.transferservice.service.impl;

import bank.transferservice.dto.event.TransferEvent;
import bank.transferservice.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private final StreamBridge streamBridge;

    @Override
    public void sendTransferEvent(TransferEvent transferEvent) {
        log.info("Sending transfer event to Kafka: {}", transferEvent);
        boolean result = streamBridge.send("transfer-out-0",transferEvent);
        log.info("Event sent: {}", result);
        log.info("Transfer event sent successfully");
    }
} 