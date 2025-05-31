package bank.loanservice.service;

import io.github.oguzalpcepni.dto.LoansDto.LoanRequest;
import io.github.oguzalpcepni.dto.LoansDto.LoanResponse;
import io.github.oguzalpcepni.dto.LoansDto.LoanStatusResponse;

import java.util.UUID;

public interface LoanService {

    LoanStatusResponse createLoan(LoanRequest loanRequest);
    int calculateCreditScore(UUID customerId);
}
