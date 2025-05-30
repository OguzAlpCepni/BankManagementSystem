package bank.loanservice.service;

import io.github.oguzalpcepni.dto.LoansDto.LoanRequest;
import io.github.oguzalpcepni.dto.LoansDto.LoanResponse;

public interface LoanService {

    LoanResponse createLoan(LoanRequest loanRequest);
}
