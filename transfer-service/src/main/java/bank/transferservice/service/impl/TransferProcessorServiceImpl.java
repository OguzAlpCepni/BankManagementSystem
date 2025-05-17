package bank.transferservice.service.impl;

import bank.transferservice.entity.Transfer;
import bank.transferservice.entity.TransferStatus;
import bank.transferservice.repository.TransferRepository;
import bank.transferservice.service.TransferProcessorService;
import io.github.oguzalpcepni.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferProcessorServiceImpl implements TransferProcessorService {

    private final TransferRepository transferRepository;

    @Override
    @Transactional
    public void processTransferResponse(TransactionEvent transactionEvent) {
        log.info("Processing transfer response event: {}", transactionEvent);
        String eventType = transactionEvent.getEventType();
        // Find the transfer by sourceAccountId and targetAccountId
        Transfer transfer = transferRepository.findById(transactionEvent.getTransferId())
                .orElseThrow(() -> new RuntimeException("Transfer not found for transaction event"));

        if (eventType.equals("COMPLETED")) {
            transfer.setStatus(TransferStatus.COMPLETED);
            transfer.setTargetAccountCredited(true);
            log.info("Transfer completed successfully: {}", transfer.getId());
        } else if (eventType.equals("FAILED")) {
            transfer.setStatus(TransferStatus.FAILED);
            transfer.setErrorMessage("Transaction failed: " + transactionEvent.getDescription());
            log.error("Transfer failed: {}", transfer.getId());
        }

        transfer.setUpdatedAt(LocalDateTime.now());
        transferRepository.save(transfer);

        log.info("Updated transfer status to {}", transfer.getStatus());
    }
}