package bank.fraudservice.service.impl;

import bank.fraudservice.repository.FraudRepository;
import bank.fraudservice.service.FraudService;
import io.github.oguzalpcepni.event.FraudCheckEvent;
import io.github.oguzalpcepni.event.FraudResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudServiceImpl implements FraudService {

    private final FraudRepository fraudRepository;
    private final StreamBridge streamBridge;

    @Override
    public void handleFraudCheck(FraudCheckEvent fraudCheckEvent) {
        log.info("event came fraud service");
        boolean passed = evaluateFraud(fraudCheckEvent.getCreditScore(), fraudCheckEvent.getAmount());
        FraudResultEvent resultEvent = new FraudResultEvent();
        resultEvent.setLoanId(fraudCheckEvent.getLoanId());
        resultEvent.setFraudCheckPassed(passed);

        boolean result = streamBridge.send("resultFraud-out-0",resultEvent);
        log.info("event send : {} ",result);




    }
    private boolean evaluateFraud(Integer creditScore, BigDecimal amount) {

        if (creditScore == null || amount == null) {
            // Eksik veri varsa reddedelim
            return false;
        }
        if (creditScore < 600) {
            return false;
        }
        // Örnek: eğer tutar 100.000’den fazla ise reddet
        java.math.BigDecimal threshold = BigDecimal.valueOf(100_000);
        if (amount.compareTo(threshold) > 0) {
            return false;
        }
        return true;
    }
}
