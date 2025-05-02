package bank.transferservice.service.impl;

import bank.transferservice.dto.TransferRequest;
import bank.transferservice.dto.TransferResponse;
import bank.transferservice.dto.event.TransferEvent;
import bank.transferservice.entity.Transfer;
import bank.transferservice.entity.TransferStatus;
import bank.transferservice.entity.TransferType;
import bank.transferservice.repository.TransferRepository;
import bank.transferservice.service.KafkaProducerService;
import bank.transferservice.service.TransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferServiceImpl implements TransferService {

    private final TransferRepository transferRepository;
    private final KafkaProducerService kafkaProducerService;

    @Override
    @Transactional
    public TransferResponse initiateTransfer(TransferRequest transferRequest) {
        log.info("Initiating transfer: {}", transferRequest);
        
        // Validate account balance
        boolean isBalanceValid = accountServiceClient.validateAccountBalance(
                transferRequest.getSourceAccountId(), transferRequest.getAmount());
        
        if (!isBalanceValid) {
            log.error("Insufficient balance for transfer");
            throw new RuntimeException("Insufficient balance for transfer");
        }
        
        // Create transfer entity
        Transfer transfer = Transfer.builder()
                .id(UUID.randomUUID())
                .sourceAccountId(transferRequest.getSourceAccountId())
                .targetAccountId(transferRequest.getTargetAccountId())
                .sourceIban(transferRequest.getSourceIban())
                .targetIban(transferRequest.getTargetIban())
                .amount(transferRequest.getAmount())
                .currency(transferRequest.getCurrency())
                .transferType(transferRequest.getTransferType())
                .status(TransferStatus.PENDING)
                .description(transferRequest.getDescription())
                .transactionReference(generateTransactionReference())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        transferRepository.save(transfer);
        log.info("Transfer created: {}", transfer);
        
        // Process the transfer based on its type
        processTransfer(transfer);
        
        return mapToTransferResponse(transfer);
    }

    @Override
    public TransferResponse getTransferById(UUID transferId) {
        log.info("Getting transfer by ID: {}", transferId);
        
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Transfer not found with ID: " + transferId));
        
        return mapToTransferResponse(transfer);
    }

    @Override
    public List<TransferResponse> getTransfersBySourceIban(String sourceIban) {
        log.info("Getting transfers by source IBAN: {}", sourceIban);
        
        List<Transfer> transfers = transferRepository.findBySourceIban(sourceIban);
        
        return transfers.stream()
                .map(this::mapToTransferResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransferResponse> getTransfersByTargetIban(String targetIban) {
        log.info("Getting transfers by target IBAN: {}", targetIban);
        
        List<Transfer> transfers = transferRepository.findByTargetIban(targetIban);
        
        return transfers.stream()
                .map(this::mapToTransferResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransferResponse> getTransfersByStatus(TransferStatus status) {
        log.info("Getting transfers by status: {}", status);
        
        List<Transfer> transfers = transferRepository.findByStatus(status);
        
        return transfers.stream()
                .map(this::mapToTransferResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransferResponse> getTransfersBySourceAccountId(UUID sourceAccountId) {
        log.info("Getting transfers by source account ID: {}", sourceAccountId);
        
        List<Transfer> transfers = transferRepository.findBySourceAccountId(sourceAccountId);
        
        return transfers.stream()
                .map(this::mapToTransferResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransferResponse> getTransfersByTargetAccountId(UUID targetAccountId) {
        log.info("Getting transfers by target account ID: {}", targetAccountId);
        
        List<Transfer> transfers = transferRepository.findByTargetAccountId(targetAccountId);
        
        return transfers.stream()
                .map(this::mapToTransferResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TransferResponse cancelTransfer(UUID transferId) {
        log.info("Cancelling transfer with ID: {}", transferId);
        
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Transfer not found with ID: " + transferId));
        
        if (transfer.getStatus() != TransferStatus.PENDING) {
            throw new RuntimeException("Only pending transfers can be cancelled");
        }
        
        transfer.setStatus(TransferStatus.CANCELLED);
        transfer.setUpdatedAt(LocalDateTime.now());
        
        transferRepository.save(transfer);
        
        // Send cancellation event
        sendTransferEvent(transfer);
        
        return mapToTransferResponse(transfer);
    }

    @Override
    @Transactional
    public TransferResponse updateTransferStatus(UUID transferId, TransferStatus status) {
        log.info("Updating transfer status: {} for transfer ID: {}", status, transferId);
        
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Transfer not found with ID: " + transferId));
        
        transfer.setStatus(status);
        transfer.setUpdatedAt(LocalDateTime.now());
        
        transferRepository.save(transfer);
        
        // Send status update event
        sendTransferEvent(transfer);
        
        return mapToTransferResponse(transfer);
    }
    
    private void processTransfer(Transfer transfer) {
        // Based on transfer type, initiate the appropriate process
        TransferType transferType = transfer.getTransferType();
        
        switch (transferType) {
            case INTERNAL:
                processInternalTransfer(transfer);
                break;
            case EFT:
                processEftTransfer(transfer);
                break;
            case SWIFT:
                processSwiftTransfer(transfer);
                break;
            case FAST:
                processFastTransfer(transfer);
                break;
            default:
                throw new RuntimeException("Unsupported transfer type: " + transferType);
        }
    }
    
    private void processInternalTransfer(Transfer transfer) {
        log.info("Processing internal transfer: {}", transfer.getId());
        
        try {
            // Debit source account
            boolean debitSuccess = accountServiceClient.debitAccount(
                    transfer.getSourceAccountId(), 
                    transfer.getAmount(), 
                    "Internal Transfer - " + transfer.getTransactionReference(), 
                    transfer.getId());
            
            if (!debitSuccess) {
                throw new RuntimeException("Failed to debit source account");
            }
            
            // Credit target account
            boolean creditSuccess = accountServiceClient.creditAccount(
                    transfer.getTargetAccountId(), 
                    transfer.getAmount(), 
                    "Internal Transfer - " + transfer.getTransactionReference(), 
                    transfer.getId());
            
            if (!creditSuccess) {
                // Compensate by refunding the source account
                accountServiceClient.creditAccount(
                        transfer.getSourceAccountId(), 
                        transfer.getAmount(), 
                        "Refund - Internal Transfer Failed - " + transfer.getTransactionReference(), 
                        transfer.getId());
                
                throw new RuntimeException("Failed to credit target account");
            }
            
            // Update transfer status
            transfer.setStatus(TransferStatus.COMPLETED);
            transfer.setUpdatedAt(LocalDateTime.now());
            transferRepository.save(transfer);
            
            // Send transfer completed event
            sendTransferEvent(transfer);
            
        } catch (Exception e) {
            log.error("Error processing internal transfer: {}", e.getMessage());
            transfer.setStatus(TransferStatus.FAILED);
            transfer.setUpdatedAt(LocalDateTime.now());
            transferRepository.save(transfer);
            
            // Send transfer failed event
            sendTransferEvent(transfer);
            
            throw new RuntimeException("Internal transfer processing failed: " + e.getMessage());
        }
    }
    
    private void processEftTransfer(Transfer transfer) {
        log.info("Processing EFT transfer: {}", transfer.getId());
        
        // Implement EFT transfer logic
        // For now, just debit the account and update status to IN_PROGRESS
        
        try {
            boolean debitSuccess = accountServiceClient.debitAccount(
                    transfer.getSourceAccountId(), 
                    transfer.getAmount(), 
                    "EFT Transfer - " + transfer.getTransactionReference(), 
                    transfer.getId());
            
            if (!debitSuccess) {
                throw new RuntimeException("Failed to debit source account for EFT");
            }
            
            transfer.setStatus(TransferStatus.IN_PROGRESS);
            transfer.setUpdatedAt(LocalDateTime.now());
            transferRepository.save(transfer);
            
            // Send the transfer event for further processing
            sendTransferEvent(transfer);
            
        } catch (Exception e) {
            log.error("Error processing EFT transfer: {}", e.getMessage());
            transfer.setStatus(TransferStatus.FAILED);
            transfer.setUpdatedAt(LocalDateTime.now());
            transferRepository.save(transfer);
            
            sendTransferEvent(transfer);
            
            throw new RuntimeException("EFT transfer processing failed: " + e.getMessage());
        }
    }
    
    private void processSwiftTransfer(Transfer transfer) {
        log.info("Processing SWIFT transfer: {}", transfer.getId());
        
        // Similar to EFT with SWIFT-specific logic
        try {
            boolean debitSuccess = accountServiceClient.debitAccount(
                    transfer.getSourceAccountId(), 
                    transfer.getAmount(), 
                    "SWIFT Transfer - " + transfer.getTransactionReference(), 
                    transfer.getId());
            
            if (!debitSuccess) {
                throw new RuntimeException("Failed to debit source account for SWIFT");
            }
            
            transfer.setStatus(TransferStatus.IN_PROGRESS);
            transfer.setUpdatedAt(LocalDateTime.now());
            transferRepository.save(transfer);
            
            sendTransferEvent(transfer);
            
        } catch (Exception e) {
            log.error("Error processing SWIFT transfer: {}", e.getMessage());
            transfer.setStatus(TransferStatus.FAILED);
            transfer.setUpdatedAt(LocalDateTime.now());
            transferRepository.save(transfer);
            
            sendTransferEvent(transfer);
            
            throw new RuntimeException("SWIFT transfer processing failed: " + e.getMessage());
        }
    }
    
    private void processFastTransfer(Transfer transfer) {
        log.info("Processing FAST transfer: {}", transfer.getId());
        
        // Fast payment system transfer logic
        try {
            boolean debitSuccess = accountServiceClient.debitAccount(
                    transfer.getSourceAccountId(), 
                    transfer.getAmount(), 
                    "FAST Transfer - " + transfer.getTransactionReference(), 
                    transfer.getId());
            
            if (!debitSuccess) {
                throw new RuntimeException("Failed to debit source account for FAST");
            }
            
            transfer.setStatus(TransferStatus.IN_PROGRESS);
            transfer.setUpdatedAt(LocalDateTime.now());
            transferRepository.save(transfer);
            
            sendTransferEvent(transfer);
            
        } catch (Exception e) {
            log.error("Error processing FAST transfer: {}", e.getMessage());
            transfer.setStatus(TransferStatus.FAILED);
            transfer.setUpdatedAt(LocalDateTime.now());
            transferRepository.save(transfer);
            
            sendTransferEvent(transfer);
            
            throw new RuntimeException("FAST transfer processing failed: " + e.getMessage());
        }
    }
    
    private void sendTransferEvent(Transfer transfer) {
        TransferEvent transferEvent = TransferEvent.builder()
                .transferId(transfer.getId())
                .sourceAccountId(transfer.getSourceAccountId())
                .targetAccountId(transfer.getTargetAccountId())
                .sourceIban(transfer.getSourceIban())
                .targetIban(transfer.getTargetIban())
                .amount(transfer.getAmount())
                .currency(transfer.getCurrency())
                .transferType(transfer.getTransferType())
                .status(transfer.getStatus())
                .description(transfer.getDescription())
                .transactionReference(transfer.getTransactionReference())
                .build();
        
        kafkaProducerService.sendTransferEvent(transferEvent);
    }
    
    private String generateTransactionReference() {
        return "TRX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private TransferResponse mapToTransferResponse(Transfer transfer) {
        return TransferResponse.builder()
                .id(transfer.getId())
                .sourceAccountId(transfer.getSourceAccountId())
                .targetAccountId(transfer.getTargetAccountId())
                .sourceIban(transfer.getSourceIban())
                .targetIban(transfer.getTargetIban())
                .amount(transfer.getAmount())
                .currency(transfer.getCurrency())
                .transferType(transfer.getTransferType())
                .status(transfer.getStatus())
                .description(transfer.getDescription())
                .transactionReference(transfer.getTransactionReference())
                .createdAt(transfer.getCreatedAt())
                .updatedAt(transfer.getUpdatedAt())
                .build();
    }
} 