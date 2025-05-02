package bank.transferservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "ACCOUNT-SERVICE")
public interface AccountServiceClient {

    @GetMapping("/api/v1/accounts/{id}")
    ResponseEntity<?> getAccountById(@PathVariable UUID id);

    @GetMapping("/api/v1/accounts/iban/{iban}")
    ResponseEntity<?> getAccountByIban(@PathVariable String iban);

    @GetMapping("/api/v1/accounts/{id}/validate")
    ResponseEntity<?> validateAccount(@PathVariable UUID id);

    @GetMapping("/api/v1/accounts/iban/{iban}/validate")
    ResponseEntity<?> validateAccountByIban(@PathVariable String iban);

    @GetMapping("/api/v1/accounts/{id}/balance-check")
    ResponseEntity<Boolean> checkBalance(
            @PathVariable UUID id,
            @RequestParam BigDecimal amount);

    @PostMapping("/api/v1/accounts/{id}/debit")
    ResponseEntity<Boolean> debitAccount(
            @PathVariable UUID id,
            @RequestParam BigDecimal amount,
            @RequestParam String description,
            @RequestParam UUID transactionId);

    @PostMapping("/api/v1/accounts/{id}/credit")
    ResponseEntity<Boolean> creditAccount(
            @PathVariable UUID id,
            @RequestParam BigDecimal amount,
            @RequestParam String description,
            @RequestParam UUID transactionId);
} 