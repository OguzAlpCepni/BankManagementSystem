package bank.accountservice.controller;

import bank.accountservice.dto.CorporateAccountDto;
import bank.accountservice.dto.request.CreateCorporateAccountRequest;
import bank.accountservice.dto.response.CorporateAccountResponse;
import bank.accountservice.dto.response.TransactionResponse;
import bank.accountservice.service.CorporateAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/corporate-accounts")
@RequiredArgsConstructor
public class CorporateAccountController {

    private final CorporateAccountService corporateAccountService;

    @PostMapping
    public ResponseEntity<CorporateAccountResponse> createAccount(@Valid @RequestBody CreateCorporateAccountRequest request) {
        return new ResponseEntity<>(corporateAccountService.createAccount(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CorporateAccountResponse> getAccountById(@PathVariable UUID id) {
        return ResponseEntity.ok(corporateAccountService.getAccountById(id));
    }

    @GetMapping
    public ResponseEntity<List<CorporateAccountResponse>> getAllAccounts() {
        return ResponseEntity.ok(corporateAccountService.getAllAccounts());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<CorporateAccountResponse>> getAccountsByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(corporateAccountService.getAccountsByCustomerId(customerId));
    }

    @GetMapping("/iban/{iban}")
    public ResponseEntity<CorporateAccountResponse> getAccountByIban(@PathVariable String iban) {
        return ResponseEntity.ok(corporateAccountService.getAccountByIban(iban));
    }

    @GetMapping("/tax-number/{taxNumber}")
    public ResponseEntity<CorporateAccountResponse> getAccountByTaxNumber(@PathVariable String taxNumber) {
        return ResponseEntity.ok(corporateAccountService.getAccountByTaxNumber(taxNumber));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CorporateAccountResponse> updateAccountDetails(
            @PathVariable UUID id,
            @Valid @RequestBody CorporateAccountDto accountDto) {
        return ResponseEntity.ok(corporateAccountService.updateAccountDetails(id, accountDto));
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<TransactionResponse> depositAmount(
            @PathVariable UUID id,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(corporateAccountService.depositAmount(id, amount));
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<TransactionResponse> withdrawAmount(
            @PathVariable UUID id,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(corporateAccountService.withdrawAmount(id, amount));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CorporateAccountResponse> updateAccountStatus(
            @PathVariable UUID id,
            @RequestParam String status) {
        return ResponseEntity.ok(corporateAccountService.updateAccountStatus(id, status));
    }
} 