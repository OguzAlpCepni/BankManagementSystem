package bank.transferservice.service.impl;

import bank.transferservice.service.KafkaProducerService;
import io.github.oguzalpcepni.event.TransferEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

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