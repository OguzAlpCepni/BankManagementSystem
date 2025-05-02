package bank.transferservice.service;

import bank.transferservice.dto.event.TransferEvent;

public interface TransferProcessorService {
    void processTransferResponse(TransferEvent transferEvent);
} 