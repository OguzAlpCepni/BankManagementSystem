package bank.loanservice.controller;

import bank.loanservice.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/loan-application")
@RequiredArgsConstructor
public class LoanApplicationController {

    private final LoanService loanService;


    @GetMapping("/credit-scre/{customerId}")
    ResponseEntity<Integer> getCreditScore(@PathVariable UUID customerId){
        Integer id = loanService.calculateCreditScore(customerId);
        return ResponseEntity.ok(id);
    }

}
