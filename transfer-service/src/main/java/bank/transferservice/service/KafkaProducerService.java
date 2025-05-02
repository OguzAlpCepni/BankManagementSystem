package bank.transferservice.service;

import bank.transferservice.dto.event.TransferEvent;

public interface KafkaProducerService {
    void sendTransferEvent(TransferEvent transferEvent);
} 