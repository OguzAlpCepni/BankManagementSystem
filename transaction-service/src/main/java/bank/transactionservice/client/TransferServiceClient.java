package bank.transactionservice.client;

import bank.transactionservice.entity.TransactionType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "transfer-service")
public interface TransferServiceClient {
    
    @PostMapping("/api/v1/transfers")
    ResponseEntity<TransferResponse> initiateTransfer(@RequestBody TransferRequest request);
    
    @PostMapping("/api/v1/transfers/{transferId}/cancel")
    ResponseEntity<TransferResponse> cancelTransfer(@PathVariable UUID transferId);
    
    record TransferRequest(
            UUID sourceAccountId,
            UUID targetAccountId,
            String sourceIban,
            String targetIban,
            BigDecimal amount,
            String currency,
            String description,
            TransactionType type,
            UUID transactionId
    ) {}
    
    record TransferResponse(
            UUID id,
            UUID sourceAccountId, 
            UUID targetAccountId,
            String sourceIban,
            String targetIban,
            BigDecimal amount,
            String currency,
            String description,
            String type,
            String status,
            UUID transactionId
    ) {}
} 