package bank.loanservice.service;

import io.github.oguzalpcepni.dto.LoansDto.LoanRequest;
import io.github.oguzalpcepni.dto.LoansDto.LoanResponse;
import io.github.oguzalpcepni.dto.LoansDto.LoanStatusResponse;

public interface LoanService {

    LoanStatusResponse createLoan(LoanRequest loanRequest);
}
