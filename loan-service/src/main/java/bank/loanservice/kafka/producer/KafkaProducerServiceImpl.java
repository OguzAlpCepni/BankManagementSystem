package bank.loanservice.kafka.producer;

import io.github.oguzalpcepni.event.LoanApplicationCreatedEvent;
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
    public void sendTransferEvent(LoanApplicationCreatedEvent loanApplicationCreatedEvent) {

            log.info("Sending loan event to Kafka: {}", loanApplicationCreatedEvent);
            boolean result = streamBridge.send("loan-out-0",loanApplicationCreatedEvent);
            log.info("Event sent: {}", result);
            log.info("loanApplicationCreatedEvent sent successfully");

    }
}
