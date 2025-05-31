package bank.transactionservice.kafka;

import bank.transactionservice.service.LoanTransactionService;
import io.github.oguzalpcepni.event.LoanApplicationCreatedEvent;
import io.github.oguzalpcepni.exceptions.type.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoanApplicationConsumer {

    private final LoanTransactionService loanTransactionService;


    @Bean
    public Consumer<LoanApplicationCreatedEvent> loanApplicationCreatedEventConsumer(TransferEventConsumer transferEventConsumer) {
        return loanApplicationCreatedEvent -> {
            log.info("loan created event come to transaction-service consumer: {}", loanApplicationCreatedEvent);
        try {
            loanTransactionService.createLoanTransaction(loanApplicationCreatedEvent);
        }catch (BusinessException businessException) {
            log.error(businessException.getMessage());
        }
        };

    }

}
