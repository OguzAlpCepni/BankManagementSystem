package bank.fraudservice.service;

import io.github.oguzalpcepni.event.FraudCheckEvent;

public interface FraudService {

    void handleFraudCheck(FraudCheckEvent fraudCheckEvent);
}
