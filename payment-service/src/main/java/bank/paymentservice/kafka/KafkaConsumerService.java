package bank.paymentservice.kafka;

import bank.paymentservice.service.PaymentService;
import io.github.oguzalpcepni.dto.payment.PaymentStatusUpdateDTO;
import io.github.oguzalpcepni.event.PaymentStatusUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {
    private final PaymentService paymentService;

    public Consumer<PaymentStatusUpdateEvent> paymentStatusUpdateEventConsumer() {
        log.info("Payment status update event consumer started");
        return paymentStatusUpdateEvent -> {
            log.info("Payment status update event received");
            PaymentStatusUpdateDTO paymentStatusUpdateDTO = new PaymentStatusUpdateDTO();
            paymentStatusUpdateDTO.setPaymentId(paymentStatusUpdateEvent.getPaymentId());
            paymentStatusUpdateDTO.setNewStatus(paymentStatusUpdateEvent.getNewStatus());
            paymentStatusUpdateDTO.setBillerResponse(paymentStatusUpdateEvent.getBillerResponse());
            paymentStatusUpdateDTO.setErrorMessage(paymentStatusUpdateEvent.getErrorMessage());
            log.info("Payment status event sending paymentService");
            paymentService.updatePaymentStatus(paymentStatusUpdateDTO);

        };
    }
}
