package bank.accountservice.service;

import bank.accountservice.entity.Account;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface AccountService {
    Optional<Account> findAccountById(UUID id);
    Optional<Account> findAccountByIban(String iban);
    
    void validateAccount(UUID id);
    void validateAccount(String iban);
    
    boolean hasEnoughBalance(UUID id, BigDecimal amount);
} 