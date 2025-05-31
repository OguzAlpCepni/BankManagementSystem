package bank.transactionservice.kafka;

import bank.transactionservice.entity.LoanTransaction;
import io.github.oguzalpcepni.event.FraudCheckEvent;
import io.github.oguzalpcepni.event.LoanUnderwritingCompletedEvent;
import io.github.oguzalpcepni.event.LoanUnderwritingRejectedEvent;
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

    public void sendFraudCompletedResultEvent(LoanTransaction loanTransaction) {
        LoanUnderwritingCompletedEvent loanUnderwritingCompletedEvent = new LoanUnderwritingCompletedEvent();
        loanUnderwritingCompletedEvent.setLoanId(loanTransaction.getLoanId());
        loanUnderwritingCompletedEvent.setCreditScore(loanTransaction.getCreditScore());
        loanUnderwritingCompletedEvent.setFraudCheckPassed(loanTransaction.getFraudCheckPassed());
        streamBridge.send("underwritingCompleted-out-0",loanUnderwritingCompletedEvent);
        log.info("event send : {} ",loanUnderwritingCompletedEvent);
    }

    public void sendFraudRejectedResultEvent(LoanTransaction loanTransaction) {
        LoanUnderwritingRejectedEvent loanUnderwritingRejectedEvent = new LoanUnderwritingRejectedEvent();
        loanUnderwritingRejectedEvent.setLoanId(loanTransaction.getLoanId());
        loanUnderwritingRejectedEvent.setCreditScore(loanTransaction.getCreditScore());
        loanUnderwritingRejectedEvent.setFraudCheckPassed(loanTransaction.getFraudCheckPassed());
        streamBridge.send("underwritingRejected-out-0",loanUnderwritingRejectedEvent);
        log.info("event send : {} ",loanUnderwritingRejectedEvent);
    }
}
