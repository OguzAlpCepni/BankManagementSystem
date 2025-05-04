package bank.transactionservice.controller;

import bank.transactionservice.dto.TransactionRequest;
import bank.transactionservice.dto.TransactionResponse;
import bank.transactionservice.entity.TransactionStatus;
import bank.transactionservice.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {
    
    private final TransactionService transactionService;
    
    @PostMapping
    public ResponseEntity<TransactionResponse> initiateTransaction(@Valid @RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.initiateTransaction(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable UUID transactionId) {
        TransactionResponse response = transactionService.getTransactionById(transactionId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/source-account/{sourceAccountId}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsBySourceAccountId(@PathVariable UUID sourceAccountId) {
        List<TransactionResponse> responses = transactionService.getTransactionsBySourceAccountId(sourceAccountId);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/target-account/{targetAccountId}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByTargetAccountId(@PathVariable UUID targetAccountId) {
        List<TransactionResponse> responses = transactionService.getTransactionsByTargetAccountId(targetAccountId);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/source-iban/{sourceIban}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsBySourceIban(@PathVariable String sourceIban) {
        List<TransactionResponse> responses = transactionService.getTransactionsBySourceIban(sourceIban);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/target-iban/{targetIban}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByTargetIban(@PathVariable String targetIban) {
        List<TransactionResponse> responses = transactionService.getTransactionsByTargetIban(targetIban);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByStatus(@PathVariable TransactionStatus status) {
        List<TransactionResponse> responses = transactionService.getTransactionsByStatus(status);
        return ResponseEntity.ok(responses);
    }
    
    @PutMapping("/{transactionId}/cancel")
    public ResponseEntity<TransactionResponse> cancelTransaction(@PathVariable UUID transactionId) {
        TransactionResponse response = transactionService.cancelTransaction(transactionId);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{transactionId}/status")
    public ResponseEntity<TransactionResponse> updateTransactionStatus(
            @PathVariable UUID transactionId, 
            @RequestParam TransactionStatus status) {
        TransactionResponse response = transactionService.updateTransactionStatus(transactionId, status);
        return ResponseEntity.ok(response);
    }
} 