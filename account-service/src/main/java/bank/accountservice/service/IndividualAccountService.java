package bank.accountservice.service;

import bank.accountservice.dto.IndividualAccountDto;
import bank.accountservice.dto.request.CreateIndividualAccountRequest;
import bank.accountservice.dto.response.IndividualAccountResponse;
import bank.accountservice.dto.response.TransactionResponse;


import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface IndividualAccountService {
    IndividualAccountResponse createAccount(CreateIndividualAccountRequest request);
    IndividualAccountResponse getAccountById(UUID id);
    List<IndividualAccountResponse> getAllAccounts();
    List<IndividualAccountResponse> getAccountsByCustomerId(UUID customerId);
    IndividualAccountResponse getAccountByIban(String iban);
    IndividualAccountResponse updateAccountDetails(UUID id, IndividualAccountDto accountDto);
    // Belirtilen hesaba para yatırır ve güncellenmiş entity'i döner
    TransactionResponse depositAmount(UUID id, BigDecimal amount);
    // Belirtilen hesaptan para çeker (overdraft kontrolü yapılmalı)
    TransactionResponse withdrawAmount(UUID id, BigDecimal amount);
    IndividualAccountResponse updateAccountStatus(UUID id, String status);
}
