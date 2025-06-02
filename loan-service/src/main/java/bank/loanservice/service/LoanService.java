package bank.loanservice.service;

import io.github.oguzalpcepni.dto.LoansDto.LoanRequest;
import io.github.oguzalpcepni.dto.LoansDto.LoanStatusResponse;
import io.github.oguzalpcepni.dto.LoansDto.LoanTransferResponse;
import io.github.oguzalpcepni.dto.accountdto.LoanAccountDto;
import io.github.oguzalpcepni.event.LoanUnderwritingCompletedEvent;
import io.github.oguzalpcepni.event.LoanUnderwritingRejectedEvent;

import java.util.UUID;

public interface LoanService {

    LoanStatusResponse createLoan(LoanRequest loanRequest);
    int calculateCreditScore(UUID customerId);
    void onUnderwritingApproved(LoanUnderwritingCompletedEvent event);
    void onUnderwritingRejected(LoanUnderwritingRejectedEvent event);
    LoanTransferResponse ApproveAndTransferMoney(UUID id, LoanAccountDto loanAccountDto);


}
