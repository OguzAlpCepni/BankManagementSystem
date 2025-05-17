package bank.transferservice.client;

import io.github.oguzalpcepni.dto.accountdto.CreditRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "ACCOUNT-SERVICE")
public interface AccountServiceClient {
    @PostMapping("/api/v1/accounts/{accountId}/debit")
    ResponseEntity<Boolean> debitAccount(
            @PathVariable UUID accountId,
            @RequestParam BigDecimal amount,
            @RequestParam String description,
            @RequestParam UUID transactionId);
} 