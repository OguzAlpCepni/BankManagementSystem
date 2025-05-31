package bank.transactionservice.kafka;

import bank.transactionservice.entity.LoanTransaction;


public interface KafkaLoanProducerService {
    void sendFraudCheckEvent(LoanTransaction loanTransaction);
    void sendFraudCompletedResultEvent(LoanTransaction loanTransaction);
    void sendFraudRejectedResultEvent(LoanTransaction loanTransaction);
}
