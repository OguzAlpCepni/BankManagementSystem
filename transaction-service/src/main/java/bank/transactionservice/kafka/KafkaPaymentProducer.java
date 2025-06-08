package bank.transactionservice.kafka;

import io.github.oguzalpcepni.event.PaymentStatusUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaPaymentProducer {


    private final StreamBridge streamBridge;


    public void publishStatusUpdate(UUID paymentId, String newStatus, String billerResponse, String errorMessage) {
        PaymentStatusUpdateEvent updateEvent = new PaymentStatusUpdateEvent(
                paymentId,
                newStatus,
                billerResponse,
                errorMessage
        );

        streamBridge.send("paymentStatusUpdate-out-0", updateEvent);
        log.info("[TransactionService] Published PaymentStatusUpdateEvent for paymentId={} with status={}", paymentId, newStatus);
    }
}
