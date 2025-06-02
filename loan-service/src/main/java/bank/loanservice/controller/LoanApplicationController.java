package bank.loanservice.controller;

import bank.loanservice.service.LoanService;
import io.github.oguzalpcepni.dto.LoansDto.LoanRequest;
import io.github.oguzalpcepni.dto.LoansDto.LoanStatusResponse;
import io.github.oguzalpcepni.dto.LoansDto.LoanTransferResponse;
import io.github.oguzalpcepni.dto.accountdto.LoanAccountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/loan-application")
@RequiredArgsConstructor
public class LoanApplicationController {

    private final LoanService loanService;


    @GetMapping("/credit-score/{customerId}")
    ResponseEntity<Integer> getCreditScore(@PathVariable UUID customerId){
        return ResponseEntity.ok(loanService.calculateCreditScore(customerId));
    }

    @PostMapping
    public ResponseEntity<LoanStatusResponse> createLoan(@RequestBody LoanRequest loanRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(loanService.createLoan(loanRequest));
    }


    @PostMapping("{loanId}/transfer")
    public ResponseEntity<LoanTransferResponse> ApproveAndTransferMoney(@PathVariable UUID loanId, @RequestBody LoanAccountDto loanAccountDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(loanService.ApproveAndTransferMoney(loanId, loanAccountDto));
    }
}
