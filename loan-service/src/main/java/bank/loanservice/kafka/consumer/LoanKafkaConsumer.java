package bank.loanservice.kafka.consumer;


import bank.loanservice.service.LoanService;
import io.github.oguzalpcepni.event.LoanUnderwritingCompletedEvent;
import io.github.oguzalpcepni.event.LoanUnderwritingRejectedEvent;
import io.github.oguzalpcepni.exceptions.type.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Consumer;

@Component
@Slf4j
@RequiredArgsConstructor
public class LoanKafkaConsumer {

    private final LoanService loanService;

    @Bean
    @Transactional
    public Consumer<LoanUnderwritingCompletedEvent> underwritingCompleted(){
        return loanUnderwritingCompletedEvent -> {
            log.info("data come to loan consumer");
            try {

                loanService.onUnderwritingApproved(loanUnderwritingCompletedEvent);
            }catch (BusinessException businessException){
                throw new BusinessException(businessException.getMessage());
            }
        };
    }

    @Bean
    @Transactional
    public Consumer<LoanUnderwritingRejectedEvent> underwritingRejected(){
        return loanUnderwritingRejectedEvent -> {
            try {
                loanService.onUnderwritingRejected(loanUnderwritingRejectedEvent);
            }catch (BusinessException businessException){
                throw new BusinessException(businessException.getMessage());
            }
        };
    }
}
