package bank.transferservice.service.impl;

import bank.transferservice.client.AccountServiceClient;
import io.github.oguzalpcepni.dto.transferdto.TransferRequest;
import io.github.oguzalpcepni.dto.transferdto.TransferResponse;
import bank.transferservice.entity.Transfer;
import bank.transferservice.entity.TransferStatus;
import bank.transferservice.entity.TransferType;
import bank.transferservice.repository.TransferRepository;
import bank.transferservice.service.KafkaProducerService;
import bank.transferservice.service.TransferService;
import io.github.oguzalpcepni.event.TransferEvent;
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
    private final AccountServiceClient accountServiceClient;
    private final KafkaProducerService kafkaProducerService;

    @Override
    @Transactional
    public TransferResponse initiateTransfer(TransferRequest transferRequest) {
        log.info("Initiating transfer: {}", transferRequest);

        // Create and save the transfer record
        Transfer transfer = createTransferEntity(transferRequest);
        transfer = transferRepository.save(transfer);

        // Attempt to debit the source account
        try {
            ResponseEntity<Boolean> debitResponse = accountServiceClient.debitAccount(
                    transferRequest.getSourceAccountId(),
                    transferRequest.getAmount(),
                    transferRequest.getDescription(),
                    transfer.getId()
            );

            if (debitResponse.getBody() != null && debitResponse.getBody()) {
                // Source account debited successfully
                transfer.setSourceAccountDebited(true);
                transfer.setStatus(TransferStatus.DEBITED);
                transfer = transferRepository.save(transfer);

                // Send the DEBITED event to Kafka
                TransferEvent transferEvent = createTransferEvent(transfer, "DEBITED");
                kafkaProducerService.sendTransferEvent(transferEvent);
                log.info("Transfer initiated successfully, ID: {}", transfer.getId());
            } else {
                // Debit failed
                transfer.setStatus(TransferStatus.FAILED);
                transfer.setErrorMessage("Failed to debit source account");
                transfer = transferRepository.save(transfer);
                log.error("Failed to debit source account, transfer ID: {}", transfer.getId());
            }
        } catch (Exception e) {
            transfer.setStatus(TransferStatus.FAILED);
            transfer.setErrorMessage("Error during debit: " + e.getMessage());
            transfer = transferRepository.save(transfer);
            log.error("Exception during debit operation: ", e);
        }

        return mapToTransferResponse(transfer);
    }

    @Override
    public TransferResponse getTransferById(UUID transferId) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new BusinessException("Transfer not found with ID: " + transferId));
        return mapToTransferResponse(transfer);
    }

    @Override
    public List<TransferResponse> getTransfersBySourceIban(String sourceIban) {
        return transferRepository.findBySourceIban(sourceIban)
                .stream()
                .map(this::mapToTransferResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransferResponse> getTransfersByTargetIban(String targetIban) {
        return transferRepository.findByTargetIban(targetIban)
                .stream()
                .map(this::mapToTransferResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransferResponse> getTransfersByStatus(TransferStatus status) {
        return transferRepository.findByStatus(status)
                .stream()
                .map(this::mapToTransferResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransferResponse> getTransfersBySourceAccountId(UUID sourceAccountId) {
        return transferRepository.findBySourceAccountId(sourceAccountId)
                .stream()
                .map(this::mapToTransferResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransferResponse> getTransfersByTargetAccountId(UUID targetAccountId) {
        return transferRepository.findByTargetAccountId(targetAccountId)
                .stream()
                .map(this::mapToTransferResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TransferResponse cancelTransfer(UUID transferId) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new BusinessException("Transfer not found with ID: " + transferId));

        if (transfer.getStatus() == TransferStatus.COMPLETED) {
            throw new BusinessException("Cannot cancel a completed transfer");
        }

        transfer.setStatus(TransferStatus.CANCELLED);
        transfer.setUpdatedAt(LocalDateTime.now());
        transfer = transferRepository.save(transfer);

        return mapToTransferResponse(transfer);
    }

    @Override
    @Transactional
    public TransferResponse updateTransferStatus(UUID transferId, TransferStatus status) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new BusinessException("Transfer not found with ID: " + transferId));

        transfer.setStatus(status);
        transfer.setUpdatedAt(LocalDateTime.now());
        transfer = transferRepository.save(transfer);

        return mapToTransferResponse(transfer);
    }

    @Override
    public String getStatusByTransferTransactionId(String transferTransactionId) {
        Transfer transfer  = transferRepository.findByTransactionReference(String.valueOf(transferTransactionId));
        return transfer.getStatus().name();
    }

    private Transfer createTransferEntity(TransferRequest request) {
        Transfer transfer = new Transfer();
        transfer.setSourceAccountId(request.getSourceAccountId());
        transfer.setTargetAccountId(request.getTargetAccountId());
        transfer.setSourceIban(request.getSourceIban());
        transfer.setTargetIban(request.getTargetIban());
        transfer.setAmount(request.getAmount());
        transfer.setCurrency(request.getCurrency());
        transfer.setDescription(request.getDescription());
        transfer.setType(TransferType.valueOf(request.getTransferType()));
        transfer.setStatus(TransferStatus.PENDING);
        transfer.setTransactionReference(request.getTransactionReference());
        transfer.setCreatedAt(LocalDateTime.now());
        transfer.setSourceAccountDebited(false);
        transfer.setTargetAccountCredited(false);
        return transfer;
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
                .description(transfer.getDescription())
                .transferType(transfer.getType().name())
                .status(transfer.getStatus().name())
                .transactionReference(transfer.getTransactionReference())
                .build();
    }

    private TransferEvent createTransferEvent(Transfer transfer, String eventType) {
        TransferEvent event = new TransferEvent();
        event.setTransferId(transfer.getId());
        event.setSourceAccountId(transfer.getSourceAccountId());
        event.setTargetAccountId(transfer.getTargetAccountId());
        event.setSourceIban(transfer.getSourceIban());
        event.setTargetIban(transfer.getTargetIban());
        event.setAmount(transfer.getAmount());
        event.setCurrency(transfer.getCurrency());
        event.setType(eventType);
        event.setStatus(transfer.getStatus().name());
        event.setDescription(transfer.getDescription());
        event.setTransactionReference(transfer.getTransactionReference());
        event.setSourceAccountDebited(transfer.isSourceAccountDebited());
        event.setTargetAccountCredited(transfer.isTargetAccountCredited());
        return event;
    }
}