package bank.transactionservice.service;

import io.github.oguzalpcepni.event.FraudResultEvent;
import io.github.oguzalpcepni.event.LoanApplicationCreatedEvent;

public interface LoanTransactionService {

    void createLoanTransaction(LoanApplicationCreatedEvent loanApplicationCreatedEvent);
    void onFraudResult(FraudResultEvent fraudResultEvent);
}
