package bank.accountservice.service;


import io.github.oguzalpcepni.dto.accountdto.CorporateAccountDto;
import io.github.oguzalpcepni.dto.accountdto.CorporateAccountResponse;
import io.github.oguzalpcepni.dto.accountdto.CreateCorporateAccountRequest;
import io.github.oguzalpcepni.dto.accountdto.TransactionResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface CorporateAccountService {

    CorporateAccountResponse createAccount(CreateCorporateAccountRequest request);
    CorporateAccountResponse getAccountById(UUID id);
    List<CorporateAccountResponse> getAllAccounts();
    List<CorporateAccountResponse> getAccountsByCustomerId(UUID customerId);
    CorporateAccountResponse getAccountByIban(String iban);
    CorporateAccountResponse getAccountByTaxNumber(String taxNumber);
    CorporateAccountResponse updateAccountDetails(UUID id, CorporateAccountDto accountDto);
    TransactionResponse depositAmount(UUID id, BigDecimal amount);
    TransactionResponse withdrawAmount(UUID id, BigDecimal amount);
    CorporateAccountResponse updateAccountStatus(UUID id, String status);
}
