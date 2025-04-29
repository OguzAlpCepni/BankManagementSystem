package bank.accountservice.service.Impl;

import bank.accountservice.entity.Account;
import bank.accountservice.entity.AccountStatus;
import bank.accountservice.repository.AccountRepository;
import bank.accountservice.service.AccountService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    public Optional<Account> findAccountById(UUID id) {
        return accountRepository.findById(id);
    }

    @Override
    public Optional<Account> findAccountByIban(String iban) {
        return accountRepository.findByIban(iban);
    }
    
    @Override
    public void validateAccount(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with id: " + id));
        
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is not active, current status: " + account.getStatus());
        }
    }
    
    @Override
    public void validateAccount(String iban) {
        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with IBAN: " + iban));
        
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is not active, current status: " + account.getStatus());
        }
    }
    
    @Override
    public boolean hasEnoughBalance(UUID id, BigDecimal amount) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with id: " + id));
        
        BigDecimal overdraftLimit = account.getOverdraftLimit() != null ? account.getOverdraftLimit() : BigDecimal.ZERO;
        BigDecimal availableBalance = account.getBalance().add(overdraftLimit);
        
        return availableBalance.compareTo(amount) >= 0;
    }
} 