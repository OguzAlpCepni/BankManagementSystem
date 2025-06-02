package bank.fraudservice.kafka;

import bank.fraudservice.service.FraudService;
import io.github.oguzalpcepni.event.FraudCheckEvent;
import io.github.oguzalpcepni.exceptions.type.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Slf4j
@RequiredArgsConstructor
public class FraudConsumer {

    private final FraudService fraudService;

    public Consumer<FraudCheckEvent> fraudCheckEventConsumer() {
        return fraudCheckEvent -> {
            log.info("event came the fraud check consumer: {}",fraudCheckEvent);
            try {
                fraudService.handleFraudCheck(fraudCheckEvent);
                log.info("event going to fraud service");
            }catch (BusinessException businessException){
                throw new BusinessException(businessException.getMessage());
            }
        };
    }

}
