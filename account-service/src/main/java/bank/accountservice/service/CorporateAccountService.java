package bank.accountservice.service;

import bank.accountservice.dto.CorporateAccountDto;
import bank.accountservice.dto.request.CreateCorporateAccountRequest;
import bank.accountservice.dto.response.CorporateAccountResponse;
import bank.accountservice.dto.response.TransactionResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface CorporateAccountService {

    CorporateAccountResponse createAccount(CreateCorporateAccountRequest request);
    CorporateAccountResponse getAccountById(UUID id);
    List<CorporateAccountResponse> getAllAccounts();
    List<CorporateAccountResponse> getAccountsByCustomerId(Long customerId);
    CorporateAccountResponse getAccountByIban(String iban);
    CorporateAccountResponse getAccountByTaxNumber(String taxNumber);
    CorporateAccountResponse updateAccountDetails(UUID id, CorporateAccountDto accountDto);
    TransactionResponse depositAmount(UUID id, BigDecimal amount);
    TransactionResponse withdrawAmount(UUID id, BigDecimal amount);
    CorporateAccountResponse updateAccountStatus(UUID id, String status);
}
