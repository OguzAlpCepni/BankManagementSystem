package bank.transferservice.controller;

import bank.transferservice.dto.TransferRequest;
import bank.transferservice.dto.TransferResponse;
import bank.transferservice.entity.TransferStatus;
import bank.transferservice.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<TransferResponse> initiateTransfer(@RequestBody TransferRequest transferRequest) {
        TransferResponse response = transferService.initiateTransfer(transferRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{transferId}")
    public ResponseEntity<TransferResponse> getTransferById(@PathVariable UUID transferId) {
        TransferResponse response = transferService.getTransferById(transferId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/source-iban/{sourceIban}")
    public ResponseEntity<List<TransferResponse>> getTransfersBySourceIban(@PathVariable String sourceIban) {
        List<TransferResponse> responses = transferService.getTransfersBySourceIban(sourceIban);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/target-iban/{targetIban}")
    public ResponseEntity<List<TransferResponse>> getTransfersByTargetIban(@PathVariable String targetIban) {
        List<TransferResponse> responses = transferService.getTransfersByTargetIban(targetIban);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TransferResponse>> getTransfersByStatus(@PathVariable TransferStatus status) {
        List<TransferResponse> responses = transferService.getTransfersByStatus(status);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/source-account/{sourceAccountId}")
    public ResponseEntity<List<TransferResponse>> getTransfersBySourceAccountId(@PathVariable UUID sourceAccountId) {
        List<TransferResponse> responses = transferService.getTransfersBySourceAccountId(sourceAccountId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/target-account/{targetAccountId}")
    public ResponseEntity<List<TransferResponse>> getTransfersByTargetAccountId(@PathVariable UUID targetAccountId) {
        List<TransferResponse> responses = transferService.getTransfersByTargetAccountId(targetAccountId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{transferId}/cancel")
    public ResponseEntity<TransferResponse> cancelTransfer(@PathVariable UUID transferId) {
        TransferResponse response = transferService.cancelTransfer(transferId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{transferId}/status")
    public ResponseEntity<TransferResponse> updateTransferStatus(
            @PathVariable UUID transferId, 
            @RequestParam TransferStatus status) {
        TransferResponse response = transferService.updateTransferStatus(transferId, status);
        return ResponseEntity.ok(response);
    }
} 