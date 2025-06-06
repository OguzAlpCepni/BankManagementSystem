package bank.transferservice.service;

import io.github.oguzalpcepni.dto.transferdto.TransferRequest;
import io.github.oguzalpcepni.dto.transferdto.TransferResponse;
import bank.transferservice.entity.TransferStatus;

import java.util.List;
import java.util.UUID;

public interface TransferService {
    TransferResponse initiateTransfer(TransferRequest transferRequest);
    TransferResponse getTransferById(UUID transferId);
    List<TransferResponse> getTransfersBySourceIban(String sourceIban);
    List<TransferResponse> getTransfersByTargetIban(String targetIban);
    List<TransferResponse> getTransfersByStatus(TransferStatus status);
    List<TransferResponse> getTransfersBySourceAccountId(UUID sourceAccountId);
    List<TransferResponse> getTransfersByTargetAccountId(UUID targetAccountId);
    TransferResponse cancelTransfer(UUID transferId);
    TransferResponse updateTransferStatus(UUID transferId, TransferStatus status);
    String getStatusByTransferTransactionId(String transferTransactionId);
}