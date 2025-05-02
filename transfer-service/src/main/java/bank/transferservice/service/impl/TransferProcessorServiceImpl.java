package bank.transferservice.service.impl;

import bank.transferservice.dto.event.TransferEvent;
import bank.transferservice.entity.Transfer;
import bank.transferservice.entity.TransferStatus;
import bank.transferservice.repository.TransferRepository;
import bank.transferservice.service.TransferProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferProcessorServiceImpl implements TransferProcessorService {

    private final TransferRepository transferRepository;

    @Override
    @Transactional
    public void processTransferResponse(TransferEvent transferEvent) {
        log.info("Processing transfer response: {}", transferEvent);
        
        UUID transferId = transferEvent.getTransferId();
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Transfer not found with ID: " + transferId));
        
        // Update the transfer status based on the event
        transfer.setStatus(transferEvent.getStatus());
        
        // Additional processing logic based on the transfer type and status
        if (transferEvent.getStatus() == TransferStatus.COMPLETED) {
            log.info("Transfer completed successfully: {}", transferId);
            // Additional logic for completed transfers
        } else if (transferEvent.getStatus() == TransferStatus.FAILED) {
            log.error("Transfer failed: {}", transferId);
            // Additional logic for failed transfers
        }
        
        transferRepository.save(transfer);
        log.info("Transfer status updated: {}", transfer);
    }
} 