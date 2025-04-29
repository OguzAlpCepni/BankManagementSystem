package bank.accountservice.controller;

import bank.accountservice.dto.IndividualAccountDto;
import bank.accountservice.dto.request.CreateIndividualAccountRequest;
import bank.accountservice.dto.response.IndividualAccountResponse;
import bank.accountservice.dto.response.TransactionResponse;
import bank.accountservice.service.IndividualAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/individual-accounts")
@RequiredArgsConstructor
public class IndividualAccountController {

    private final IndividualAccountService individualAccountService;

    @PostMapping
    public ResponseEntity<IndividualAccountResponse> createAccount(@Valid @RequestBody CreateIndividualAccountRequest request) {
        return new ResponseEntity<>(individualAccountService.createAccount(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IndividualAccountResponse> getAccountById(@PathVariable UUID id) {
        return ResponseEntity.ok(individualAccountService.getAccountById(id));
    }

    @GetMapping
    public ResponseEntity<List<IndividualAccountResponse>> getAllAccounts() {
        return ResponseEntity.ok(individualAccountService.getAllAccounts());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<IndividualAccountResponse>> getAccountsByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(individualAccountService.getAccountsByCustomerId(customerId));
    }

    @GetMapping("/iban/{iban}")
    public ResponseEntity<IndividualAccountResponse> getAccountByIban(@PathVariable String iban) {
        return ResponseEntity.ok(individualAccountService.getAccountByIban(iban));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IndividualAccountResponse> updateAccountDetails(
            @PathVariable UUID id,
            @Valid @RequestBody IndividualAccountDto accountDto) {
        return ResponseEntity.ok(individualAccountService.updateAccountDetails(id, accountDto));
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<TransactionResponse> depositAmount(
            @PathVariable UUID id,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(individualAccountService.depositAmount(id, amount));
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<TransactionResponse> withdrawAmount(
            @PathVariable UUID id,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(individualAccountService.withdrawAmount(id, amount));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<IndividualAccountResponse> updateAccountStatus(
            @PathVariable UUID id,
            @RequestParam String status) {
        return ResponseEntity.ok(individualAccountService.updateAccountStatus(id, status));
    }
} 