package bank.accountservice.service;


import io.github.oguzalpcepni.dto.accountdto.CreateIndividualAccountRequest;
import io.github.oguzalpcepni.dto.accountdto.IndividualAccountDto;
import io.github.oguzalpcepni.dto.accountdto.IndividualAccountResponse;
import io.github.oguzalpcepni.dto.accountdto.TransactionResponse;

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
