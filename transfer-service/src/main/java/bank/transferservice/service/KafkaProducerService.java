package bank.transferservice.service;

import io.github.oguzalpcepni.event.TransferEvent;

public interface KafkaProducerService {
    void sendTransferEvent(TransferEvent transferEvent);
} 