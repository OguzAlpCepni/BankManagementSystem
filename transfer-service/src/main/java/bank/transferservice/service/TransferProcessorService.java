package bank.transferservice.service;

import io.github.oguzalpcepni.event.TransactionEvent;
import io.github.oguzalpcepni.event.TransferEvent;

public interface TransferProcessorService {
    void processTransferResponse(TransactionEvent transactionEvent);
} 