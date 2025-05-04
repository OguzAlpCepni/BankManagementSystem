package bank.transactionservice.kafka;

import bank.transactionservice.entity.Transaction;

public interface KafkaProducerService {
    void sendTransactionCreatedEvent(Transaction transaction);
    void sendTransactionCompletedEvent(Transaction transaction);
    void sendTransactionFailedEvent(Transaction transaction);

}
