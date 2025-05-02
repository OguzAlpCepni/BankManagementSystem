package bank.accountservice.controller;

import bank.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getAccountById(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.findAccountById(id));
    }

    @GetMapping("/iban/{iban}")
    public ResponseEntity<?> getAccountByIban(@PathVariable String iban) {
        return ResponseEntity.ok(accountService.findAccountByIban(iban));
    }

    @GetMapping("/{id}/validate")
    public ResponseEntity<?> validateAccount(@PathVariable UUID id) {
        accountService.validateAccount(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/iban/{iban}/validate")
    public ResponseEntity<?> validateAccountByIban(@PathVariable String iban) {
        accountService.validateAccount(iban);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/balance-check")
    public ResponseEntity<Boolean> checkBalance(
            @PathVariable UUID id,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(accountService.hasEnoughBalance(id, amount));
    }
    
    @PostMapping("/{id}/debit")
    public ResponseEntity<Boolean> debitAccount(// para çek
            @PathVariable UUID id,
            @RequestParam BigDecimal amount,
            @RequestParam String description,
            @RequestParam UUID transactionId) {
        return ResponseEntity.ok(accountService.debitAccount(id, amount, description, transactionId));
    }
    
    @PostMapping("/{id}/credit")
    public ResponseEntity<Boolean> creditAccount(//Hesaba para yatırm
            @PathVariable UUID id,
            @RequestParam BigDecimal amount,
            @RequestParam String description,
            @RequestParam UUID transactionId) {
        return ResponseEntity.ok(accountService.creditAccount(id, amount, description, transactionId));
    }
} 