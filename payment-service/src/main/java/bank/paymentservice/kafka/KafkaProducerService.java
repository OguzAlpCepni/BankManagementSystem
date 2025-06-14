package bank.paymentservice.kafka;

import bank.paymentservice.entity.Payment;

import io.github.oguzalpcepni.event.PaymentRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final StreamBridge streamBridge;

    public void publishPaymentRequested(Payment payment) {
        PaymentRequestedEvent event = new PaymentRequestedEvent();
        event.setPaymentId(payment.getId());
        event.setUserId(payment.getUserId());
        event.setAccountId(payment.getAccountId());
        event.setBillType(payment.getBillType().name());
        event.setBillerCode(payment.getBillerCode());
        event.setSubscriberNumber(payment.getSubscriberNumber());
        event.setAmount(payment.getAmount());
        event.setBillAmount(payment.getBillAmount());
        event.setCommissionAmount(payment.getCommissionAmount());
        event.setPaymentMethod(payment.getPaymentMethod().name());
        event.setDueDate(payment.getDueDate());
        event.setRequestedAt(payment.getCreatedAt());
        log.info("event ready to requested: {}", event);
        boolean result = streamBridge.send("paymentRequested-out-0",event);
        log.info("Payment event send: {}", result);
    }
}
