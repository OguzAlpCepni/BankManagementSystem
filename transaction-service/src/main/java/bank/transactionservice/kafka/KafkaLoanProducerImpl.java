package bank.transactionservice.kafka;

import bank.transactionservice.entity.LoanTransaction;
import io.github.oguzalpcepni.event.FraudCheckEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaLoanProducerImpl implements KafkaLoanProducerService{

    private final StreamBridge streamBridge;

    @Override
    public void sendFraudCheckEvent(LoanTransaction loanTransaction) {
        FraudCheckEvent fraudCheckEvent = new FraudCheckEvent();
        fraudCheckEvent.setLoanId(loanTransaction.getLoanId());
        fraudCheckEvent.setAmount(loanTransaction.getAmount());
        fraudCheckEvent.setCreditScore(loanTransaction.getCreditScore());
        boolean result = streamBridge.send("checkFraud-out-0",fraudCheckEvent);
        log.info("event send : {} ",result);

    }
}
