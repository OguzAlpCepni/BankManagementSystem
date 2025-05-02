package bank.transferservice.service.impl;

import bank.transferservice.client.AccountServiceClient;
import bank.transferservice.dto.TransferRequest;
import bank.transferservice.dto.TransferResponse;
import bank.transferservice.dto.event.TransferEvent;
import bank.transferservice.entity.Transfer;
import bank.transferservice.entity.TransferStatus;
import bank.transferservice.entity.TransferType;
import bank.transferservice.repository.TransferRepository;
import bank.transferservice.service.KafkaProducerService;
import bank.transferservice.service.TransferService;
import io.github.oguzalpcepni.exceptions.type.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final AccountServiceClient accountServiceClient;

    @Override
    @Transactional
    public TransferResponse initiateTransfer(TransferRequest transferRequest) {
        log.info("Initiating transfer: {}", transferRequest);

        //Kaynak hesapta yeterli bakiye kontrolü
        validateAccountBalance(transferRequest.getSourceAccountId(),transferRequest.getAmount());
        
        // Create transfer entity
        Transfer transfer = new Transfer();
        transfer.setId(UUID.randomUUID());
        transfer.setSourceAccountId(transferRequest.getSourceAccountId());
        transfer.setTargetAccountId(transferRequest.getTargetAccountId());
        transfer.setSourceIban(transferRequest.getSourceIban());
        transfer.setTargetIban(transferRequest.getTargetIban());
        transfer.setAmount(transferRequest.getAmount());
        transfer.setCurrency(transferRequest.getCurrency());
        transfer.setType(resolveTransferType(transferRequest));
        transfer.setStatus(TransferStatus.PENDING);
        transfer.setDescription(transferRequest.getDescription());
        transfer.setTransactionReference(generateTransactionReference());
        transfer.setCreatedAt(LocalDateTime.now());
        transfer.setUpdatedAt(LocalDateTime.now());
        transfer.setSourceAccountDebited(false);
        transfer.setTargetAccountCredited(false);
        
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
                .orElseThrow(() -> new BusinessException("Transfer not found with ID: " + transferId));
        
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
                .orElseThrow(() -> new BusinessException("Transfer not found with ID: " + transferId));
        
        if (transfer.getStatus() != TransferStatus.PENDING) {
            throw new BusinessException("Only pending transfers can be cancelled");
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
                .orElseThrow(() -> new BusinessException("Transfer not found with ID: " + transferId));
        
        transfer.setStatus(status);
        transfer.setUpdatedAt(LocalDateTime.now());
        
        transferRepository.save(transfer);
        
        // Send status update event
        sendTransferEvent(transfer);
        
        return mapToTransferResponse(transfer);
    }
    
    private void processTransfer(Transfer transfer) {
        // Based on transfer type, initiate the appropriate process
        TransferType transferType = transfer.getType();
        
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
                throw new BusinessException("Unsupported transfer type: " + transferType);
        }
    }
    
    private void processInternalTransfer(Transfer transfer) {
        log.info("Processing internal transfer: {}", transfer.getId());
        
        try {
            // 1. Kaynak hesaptan para çek
            ResponseEntity<Boolean> debitResponse = accountServiceClient.debitAccount(
                    transfer.getSourceAccountId(), 
                    transfer.getAmount(), 
                    "Internal Transfer - " + transfer.getTransactionReference(), 
                    transfer.getId());
            //  çekme başarısızsa hata fırlat
            if (debitResponse.getBody() == null || !debitResponse.getBody()) {
                throw new BusinessException("Failed to debit source account");
            }
            
            // Update transfer to indicate source account has been debited
            transfer.setSourceAccountDebited(true);
            transferRepository.save(transfer);
            
            // Send event for saga orchestration
            sendTransferEvent(transfer);

            // 3. Hedef hesaba para yatır
            ResponseEntity<Boolean> creditResponse = accountServiceClient.creditAccount(
                    transfer.getTargetAccountId(), 
                    transfer.getAmount(), 
                    "Internal Transfer - " + transfer.getTransactionReference(), 
                    transfer.getId());
            
            if (creditResponse.getBody() == null || !creditResponse.getBody()) {
                // Compensating transaction - refund the source account
                accountServiceClient.creditAccount(
                        transfer.getSourceAccountId(), 
                        transfer.getAmount(), 
                        "Refund - Internal Transfer Failed - " + transfer.getTransactionReference(), 
                        transfer.getId());
                
                transfer.setSourceAccountDebited(false);
                throw new BusinessException("Failed to credit target account");
            }
            
            // Update transfer to indicate target account has been credited
            transfer.setTargetAccountCredited(true);
            
            // Update transfer status to completed
            transfer.setStatus(TransferStatus.COMPLETED);
            transfer.setCompletedAt(LocalDateTime.now());
            transfer.setUpdatedAt(LocalDateTime.now());
            transferRepository.save(transfer);
            
            // Send transfer completed event
            sendTransferEvent(transfer);
            
        } catch (BusinessException e) {
            log.error("Error processing internal transfer: {}", e.getMessage());
            transfer.setStatus(TransferStatus.FAILED);
            transfer.setErrorMessage(e.getMessage());
            transfer.setUpdatedAt(LocalDateTime.now());
            transferRepository.save(transfer);
            
            // Send transfer failed event
            sendTransferEvent(transfer);
            
            throw new BusinessException("Internal transfer processing failed: " + e.getMessage());
        }
    }
    
    private void processEftTransfer(Transfer transfer) {
        log.info("Processing EFT transfer: {}", transfer.getId());
        
        try {
            // Debit source account using Feign Client
            ResponseEntity<Boolean> debitResponse = accountServiceClient.debitAccount(
                    transfer.getSourceAccountId(), 
                    transfer.getAmount(), 
                    "EFT Transfer - " + transfer.getTransactionReference(), 
                    transfer.getId());
            
            if (debitResponse.getBody() == null || !debitResponse.getBody()) {
                throw new BusinessException("Failed to debit source account for EFT");
            }
            
            // In real-world scenario, we would initiate an EFT via external banking API
            // For now, just mark as pending and emit event
            
            transfer.setSourceAccountDebited(true);
            transfer.setStatus(TransferStatus.PENDING);
            transfer.setUpdatedAt(LocalDateTime.now());
            transferRepository.save(transfer);
            
            // Send the transfer event for further processing by Transaction Management Service
            sendTransferEvent(transfer);
            
        } catch (Exception e) {
            log.error("Error processing EFT transfer: {}", e.getMessage());
            transfer.setStatus(TransferStatus.FAILED);
            transfer.setErrorMessage(e.getMessage());
            transfer.setUpdatedAt(LocalDateTime.now());
            transferRepository.save(transfer);
            
            sendTransferEvent(transfer);
            
            throw new BusinessException("EFT transfer processing failed: " + e.getMessage());
        }
    }
    
    private void processSwiftTransfer(Transfer transfer) {
        log.info("Processing SWIFT transfer: {}", transfer.getId());
        
        try {
            // Debit source account using Feign Client
            ResponseEntity<Boolean> debitResponse = accountServiceClient.debitAccount(
                    transfer.getSourceAccountId(), 
                    transfer.getAmount(), 
                    "SWIFT Transfer - " + transfer.getTransactionReference(), 
                    transfer.getId());
            
            if (debitResponse.getBody() == null || !debitResponse.getBody()) {
                throw new BusinessException("Failed to debit source account for SWIFT");
            }
            
            // In real-world scenario, we would initiate a SWIFT transfer via external banking API
            // For now, just mark as pending and emit event
            
            transfer.setSourceAccountDebited(true);
            transfer.setStatus(TransferStatus.PENDING);
            transfer.setUpdatedAt(LocalDateTime.now());
            transferRepository.save(transfer);
            
            sendTransferEvent(transfer);
            
        } catch (BusinessException e) {
            log.error("Error processing SWIFT transfer: {}", e.getMessage());
            transfer.setStatus(TransferStatus.FAILED);
            transfer.setErrorMessage(e.getMessage());
            transfer.setUpdatedAt(LocalDateTime.now());
            transferRepository.save(transfer);
            
            sendTransferEvent(transfer);
            
            throw new BusinessException("SWIFT transfer processing failed: " + e.getMessage());
        }
    }
    
    private void processFastTransfer(Transfer transfer) {
        log.info("Processing FAST transfer: {}", transfer.getId());
        
        try {
            // Debit source account using Feign Client
            ResponseEntity<Boolean> debitResponse = accountServiceClient.debitAccount(
                    transfer.getSourceAccountId(), 
                    transfer.getAmount(), 
                    "FAST Transfer - " + transfer.getTransactionReference(), 
                    transfer.getId());
            
            if (debitResponse.getBody() == null || !debitResponse.getBody()) {
                throw new BusinessException("Failed to debit source account for FAST");
            }
            
            // In real-world scenario, we would initiate a FAST transfer via external banking API
            // For now, just mark as pending and emit event
            
            transfer.setSourceAccountDebited(true);
            transfer.setStatus(TransferStatus.PENDING);
            transfer.setUpdatedAt(LocalDateTime.now());
            transferRepository.save(transfer);
            
            sendTransferEvent(transfer);
            
        } catch (BusinessException e) {
            log.error("Error processing FAST transfer: {}", e.getMessage());
            transfer.setStatus(TransferStatus.FAILED);
            transfer.setErrorMessage(e.getMessage());
            transfer.setUpdatedAt(LocalDateTime.now());
            transferRepository.save(transfer);
            
            sendTransferEvent(transfer);
            
            throw new BusinessException("FAST transfer processing failed: " + e.getMessage());
        }
    }
    
    private void sendTransferEvent(Transfer transfer) {
        TransferEvent transferEvent = new TransferEvent();
        transferEvent.setTransferId(transfer.getId());
        transferEvent.setSourceAccountId(transfer.getSourceAccountId());
        transferEvent.setTargetAccountId(transfer.getTargetAccountId());
        transferEvent.setSourceIban(transfer.getSourceIban());
        transferEvent.setTargetIban(transfer.getTargetIban());
        transferEvent.setAmount(transfer.getAmount());
        transferEvent.setCurrency(transfer.getCurrency());
        transferEvent.setType(transfer.getType());
        transferEvent.setStatus(transfer.getStatus());
        transferEvent.setDescription(transfer.getDescription());
        transferEvent.setTransactionReference(transfer.getTransactionReference());
        transferEvent.setSourceAccountDebited(transfer.getSourceAccountDebited());
        transferEvent.setTargetAccountCredited(transfer.getTargetAccountCredited());
        
        kafkaProducerService.sendTransferEvent(transferEvent);
    }
    
    private TransferType resolveTransferType(TransferRequest request) {
        // Basit mantık: DTO'daki transferType'ı entity'deki type'a çeviriyoruz
        return TransferType.valueOf(request.getTransferType().name());
    }
    
    private String generateTransactionReference() {
        return "TRX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private TransferResponse mapToTransferResponse(Transfer transfer) {
        TransferResponse response = new TransferResponse();
        response.setId(transfer.getId());
        response.setSourceAccountId(transfer.getSourceAccountId());
        response.setTargetAccountId(transfer.getTargetAccountId());
        response.setSourceIban(transfer.getSourceIban());
        response.setTargetIban(transfer.getTargetIban());
        response.setAmount(transfer.getAmount());
        response.setCurrency(transfer.getCurrency());
        response.setTransferType(transfer.getType());
        response.setStatus(transfer.getStatus());
        response.setDescription(transfer.getDescription());
        response.setTransactionReference(transfer.getTransactionReference());
        response.setCreatedAt(transfer.getCreatedAt());
        response.setUpdatedAt(transfer.getUpdatedAt());
        return response;
    }
    private void validateAccountBalance(UUID sourceAccountId, BigDecimal amount) {
        log.debug("Validating balance for account: {} with amount: {}", sourceAccountId, amount);
        ResponseEntity<Boolean> balanceCheckResponse = accountServiceClient.checkBalance(
                sourceAccountId,
                amount
        );

        if (balanceCheckResponse.getBody() == null || !balanceCheckResponse.getBody()) {
            log.error("Insufficient balance in account: {} for amount: {}", sourceAccountId, amount);
            throw new BusinessException("Insufficient balance for transfer");
        }
    }
} 