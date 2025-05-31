package bank.transactionservice.kafka;

import bank.transactionservice.entity.LoanTransaction;
import bank.transactionservice.entity.Transaction;

public interface KafkaProducerService {
    void sendTransactionCompletedEvent(Transaction transaction);
    void sendTransactionFailedEvent(Transaction transaction);


}
