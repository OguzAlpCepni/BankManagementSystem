package bank.transactionservice.kafka;

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

    @Bean
    public Consumer<PaymentRequestedEvent> paymentRequestedConsumer(){
        return paymentRequestedEvent -> {
            log.info("Payment requested recieved in transaction service: " + paymentRequestedEvent);


        };
    }
}
