package bank.fraudservice.service;

import io.github.oguzalpcepni.event.FraudCheckEvent;

public interface FraudService {

    public void handleFraudCheck(FraudCheckEvent fraudCheckEvent);
}
