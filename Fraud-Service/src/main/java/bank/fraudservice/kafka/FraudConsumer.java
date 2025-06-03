package bank.fraudservice.kafka;

import bank.fraudservice.service.FraudService;
import io.github.oguzalpcepni.event.FraudCheckEvent;
import io.github.oguzalpcepni.exceptions.type.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Slf4j
@RequiredArgsConstructor
public class FraudConsumer {

    private final FraudService fraudService;

    @Bean
    public Consumer<FraudCheckEvent> fraudCheckEventConsumer() {
        return fraudCheckEvent -> {
            log.info("event came the fraud check consumer: {}",fraudCheckEvent);
            try {
                fraudService.handleFraudCheck(fraudCheckEvent);
            }catch (BusinessException businessException){
                throw new BusinessException(businessException.getMessage());
            }
        };
    }

}
