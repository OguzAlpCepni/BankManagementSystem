package bank.transferservice.service.impl;

import bank.transferservice.client.AccountServiceClient;
import bank.transferservice.dto.event.TransferEvent;
import bank.transferservice.entity.Transfer;
import bank.transferservice.entity.TransferStatus;
import bank.transferservice.entity.TransferType;
import bank.transferservice.repository.TransferRepository;
import bank.transferservice.service.KafkaProducerService;
import bank.transferservice.service.TransferProcessorService;
import io.github.oguzalpcepni.exceptions.type.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferProcessorServiceImpl implements TransferProcessorService {

    private final TransferRepository transferRepository;
    private final AccountServiceClient accountServiceClient;

    @Override
    @Transactional
    public void processTransferResponse(TransferEvent transferEvent) {
        log.info("Processing transfer response: {}", transferEvent);
        
        UUID transferId = transferEvent.getTransferId();
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new BusinessException("Transfer not found with ID: " + transferId));
        
        // Update the transfer based on the event
        updateTransferFromEvent(transfer, transferEvent);
        
        // Process based on transfer status from the event
        if (transferEvent.getStatus() == TransferStatus.COMPLETED) {
            processCompletedTransfer(transfer, transferEvent);
        } else if (transferEvent.getStatus() == TransferStatus.FAILED) {
            processFailedTransfer(transfer, transferEvent);
        } else if (transferEvent.getStatus() == TransferStatus.CANCELLED) {
            processCancelledTransfer(transfer, transferEvent);
        }
        
        // Save the updated transfer
        transferRepository.save(transfer);
        log.info("Transfer updated: {}", transfer);
    }
    
    private void updateTransferFromEvent(Transfer transfer, TransferEvent event) {
        transfer.setStatus(event.getStatus());
        transfer.setUpdatedAt(LocalDateTime.now());
        
        if (event.getSourceAccountDebited() != null) {
            transfer.setSourceAccountDebited(event.getSourceAccountDebited());
        }
        
        if (event.getTargetAccountCredited() != null) {
            transfer.setTargetAccountCredited(event.getTargetAccountCredited());
        }
        
        if (event.getStatus() == TransferStatus.COMPLETED) {
            transfer.setCompletedAt(LocalDateTime.now());
        }
    }
    
    private void processCompletedTransfer(Transfer transfer, TransferEvent event) {
        log.info("Processing completed transfer: {}", transfer.getId());
        
        // If this is an external transfer (EFT, SWIFT, FAST) that has been completed,
        // we need to credit the target account if it's an internal account
        if (transfer.getType() != TransferType.INTERNAL && !transfer.getTargetAccountCredited()) {
            try {
                if (transfer.getTargetAccountId() != null) {
                    ResponseEntity<Boolean> creditResponse = accountServiceClient.creditAccount(
                            transfer.getTargetAccountId(),
                            transfer.getAmount(),
                            "External Transfer Credit - " + transfer.getTransactionReference(),
                            transfer.getId());
                    
                    if (creditResponse.getBody() == null || !creditResponse.getBody()) {
                        log.error("Failed to credit target account for completed external transfer");
                        throw new BusinessException("Failed to credit target account");
                    }
                    
                    transfer.setTargetAccountCredited(true);
                }
            } catch (BusinessException e) {
                log.error("Error crediting target account: {}", e.getMessage());
                throw new BusinessException("Error completing transfer: " + e.getMessage());
            }
        }
    }
    
    private void processFailedTransfer(Transfer transfer, TransferEvent event) {
        log.info("Processing failed transfer: {}", transfer.getId());
        
        // If source account was debited but target wasn't credited, we need to refund
        if (transfer.getSourceAccountDebited() && !transfer.getTargetAccountCredited()) {
            try {
                ResponseEntity<Boolean> refundResponse = accountServiceClient.creditAccount(
                        transfer.getSourceAccountId(),
                        transfer.getAmount(),
                        "Refund for Failed Transfer - " + transfer.getTransactionReference(),
                        transfer.getId());
                
                if (refundResponse.getBody() == null || !refundResponse.getBody()) {
                    log.error("Failed to refund source account for failed transfer");
                } else {
                    transfer.setSourceAccountDebited(false);
                    transfer.setStatus(TransferStatus.REFUNDED);
                }
            } catch (BusinessException e) {
                log.error("Error refunding source account: {}", e.getMessage());
            }
        }
    }
    
    private void processCancelledTransfer(Transfer transfer, TransferEvent event) {
        log.info("Processing cancelled transfer: {}", transfer.getId());
        
        // If source account was debited but transfer was cancelled, we need to refund
        if (transfer.getSourceAccountDebited()) {
            try {
                ResponseEntity<Boolean> refundResponse = accountServiceClient.creditAccount(
                        transfer.getSourceAccountId(),
                        transfer.getAmount(),
                        "Refund for Cancelled Transfer - " + transfer.getTransactionReference(),
                        transfer.getId());
                
                if (refundResponse.getBody() == null || !refundResponse.getBody()) {
                    log.error("Failed to refund source account for cancelled transfer");
                } else {
                    transfer.setSourceAccountDebited(false);
                    transfer.setStatus(TransferStatus.REFUNDED);
                }
            } catch (BusinessException e) {
                log.error("Error refunding source account: {}", e.getMessage());
            }
        }
    }
} 