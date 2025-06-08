package bank.transactionservice.kafka;

import bank.transactionservice.service.PaymentService;
import io.github.oguzalpcepni.event.PaymentRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaPaymentConsumer {
    private final PaymentService paymentService;

    @Bean
    public Consumer<PaymentRequestedEvent> paymentRequestedConsumer() {
        return paymentRequestedEvent -> {
            log.info("Payment requested recieved in transaction service: " + paymentRequestedEvent);
            try {
                paymentService.handlePaymentRequested(paymentRequestedEvent);
            } catch (Exception ex) {
                log.error("Unexpected error while processing PaymentRequestedEvent: {}", ex.getMessage(), ex);
                // buraya Dead Letter Topic'e yollama ya da başka recovery işlemi de ekleyebilirsin
            }
        };
    }
}
