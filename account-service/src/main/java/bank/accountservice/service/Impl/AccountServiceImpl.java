package bank.accountservice.service.Impl;

import bank.accountservice.entity.Account;
import bank.accountservice.entity.AccountStatus;
import bank.accountservice.repository.AccountRepository;
import bank.accountservice.service.AccountService;
import io.github.oguzalpcepni.dto.accountdto.AccountDto;
import io.github.oguzalpcepni.exceptions.type.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    public Optional<AccountDto> findAccountById(UUID id) {
        Optional<Account> account = accountRepository.findById(id);
        if (account.isPresent()) {
            Account acc = account.get();
            AccountDto accountDto = new AccountDto();

            // Map fields from Account entity to AccountDto
            accountDto.setId(acc.getId());
            accountDto.setIban(acc.getIban());
            accountDto.setType(acc.getType().name());  // Convert enum to string
            accountDto.setStatus(acc.getStatus().name());  // Convert enum to string
            accountDto.setBalance(acc.getBalance());
            accountDto.setOverdraftLimit(acc.getOverdraftLimit());
            accountDto.setCurrency(acc.getCurrency().name());  // Convert enum to string
            accountDto.setCustomerId(acc.getCustomerId());

            // Map fields from BaseEntity
            accountDto.setVersion(acc.getVersion());
            accountDto.setCreatedAt(acc.getCreatedAt());
            accountDto.setUpdatedAt(acc.getUpdatedAt());

            return Optional.of(accountDto);
        }
        return Optional.empty();
    }

    @Override
    public Optional<AccountDto> findAccountByIban(String iban) {
        Optional<Account> account = accountRepository.findByIban(iban);
        if (account.isPresent()) {
            Account acc = account.get();
            AccountDto accountDto = new AccountDto();

            // Map fields from Account entity to AccountDto
            accountDto.setId(acc.getId());
            accountDto.setIban(acc.getIban());
            accountDto.setType(acc.getType().name());  // Convert enum to string
            accountDto.setStatus(acc.getStatus().name());  // Convert enum to string
            accountDto.setBalance(acc.getBalance());
            accountDto.setOverdraftLimit(acc.getOverdraftLimit());
            accountDto.setCurrency(acc.getCurrency().name());  // Convert enum to string
            accountDto.setCustomerId(acc.getCustomerId());

            // Map fields from BaseEntity
            accountDto.setVersion(acc.getVersion());
            accountDto.setCreatedAt(acc.getCreatedAt());
            accountDto.setUpdatedAt(acc.getUpdatedAt());

            return Optional.of(accountDto);
        }
        return Optional.empty();

    }
    
    @Override
    public void validateAccount(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Account not found with id: " + id));
        
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("Account is not active, current status: " + account.getStatus());
        }
    }
    
    @Override
    public void validateAccount(String iban) {
        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new BusinessException("Account not found with IBAN: " + iban));
        
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is not active, current status: " + account.getStatus());
        }
    }
    
    @Override
    public boolean hasEnoughBalance(UUID id, BigDecimal amount) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Account not found with id: " + id));
        
        BigDecimal overdraftLimit = account.getOverdraftLimit() != null ? account.getOverdraftLimit() : BigDecimal.ZERO;
        BigDecimal availableBalance = account.getBalance().add(overdraftLimit);
        
        return availableBalance.compareTo(amount) >= 0;
    }
    
    @Override
    @Transactional
    public boolean debitAccount(UUID id, BigDecimal amount, String description, UUID transactionId) {
        log.info("Debit operation - Account ID: {}, Amount: {}, Description: {}, Transaction ID: {}", 
                id, amount, description, transactionId);
        
        try {
            Account account = accountRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("Account not found with id: " + id));
                    
            // Hesabın aktif olup olmadığını kontrol et
            if (account.getStatus() != AccountStatus.ACTIVE) {
                log.error("Cannot debit from inactive account: {}, status: {}", id, account.getStatus());
                return false;
            }
            
            // Yeterli bakiye kontrolü
            BigDecimal overdraftLimit = account.getOverdraftLimit() != null ? account.getOverdraftLimit() : BigDecimal.ZERO;
            BigDecimal availableBalance = account.getBalance().add(overdraftLimit);
            
            if (availableBalance.compareTo(amount) < 0) {
                log.error("Insufficient balance for account: {}, required: {}, available: {}", 
                        id, amount, availableBalance);
                return false;
            }
            
            // Hesaptan para çekme işlemi
            BigDecimal newBalance = account.getBalance().subtract(amount);
            account.setBalance(newBalance);
            account.setUpdatedAt(LocalDateTime.now());
            
            accountRepository.save(account);
            
            // Burada hareket kaydı oluşturulabilir (Transaction entity)
            // saveTransactionRecord(account, amount, description, transactionId, "DEBIT");
            
            log.info("Successfully debited {} from account {}, new balance: {}", 
                    amount, id, newBalance);
            return true;
            
        } catch (Exception e) {
            log.error("Error during debit operation for account: {}", id, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean creditAccount(String iban, BigDecimal amount, String description, UUID transactionId) {
        log.info("Credit operation - Account ID: {}, Amount: {}, Description: {}, Transaction ID: {}",
                iban, amount, description, transactionId);
        
        try {
            Account account = accountRepository.findByIban(iban)
                    .orElseThrow(() -> new BusinessException("Account not found with id: " + iban));
                    
            // Hesabın aktif olup olmadığını kontrol et
            if (account.getStatus() != AccountStatus.ACTIVE) {
                log.error("Cannot credit to inactive account: {}, status: {}", iban, account.getStatus());
                return false;
            }
            
            // Hesaba para yatırma işlemi
            BigDecimal newBalance = account.getBalance().add(amount);
            account.setBalance(newBalance);
            account.setUpdatedAt(LocalDateTime.now());
            
            accountRepository.save(account);
            
            // Burada hareket kaydı oluşturulabilir (Transaction entity)
            // saveTransactionRecord(account, amount, description, transactionId, "CREDIT");
            
            log.info("Successfully credited {} to account {}, new balance: {}", 
                    amount, iban, newBalance);
            return true;
            
        } catch (Exception e) {
            log.error("Error during credit operation for account: {}", iban, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean compensateDebit(String iban, BigDecimal amount, String description, UUID transactionId) {
        log.info("Compensate debit operation - Account IBAN: {}, Amount: {}, Description: {}, Transaction ID: {}",
                iban, amount, description, transactionId);
        
        try {
            Account account = accountRepository.findByIban(iban)
                    .orElseThrow(() -> new BusinessException("Account not found with IBAN: " + iban));
                    
            // Hesaba telafi amaçlı para yatırma işlemi
            BigDecimal newBalance = account.getBalance().add(amount);
            account.setBalance(newBalance);
            account.setUpdatedAt(LocalDateTime.now());
            
            accountRepository.save(account);
            
            // Burada hareket kaydı oluşturulabilir (Transaction entity)
            // saveTransactionRecord(account, amount, description, transactionId, "COMPENSATION");
            
            log.info("Successfully compensated {} to account {}, new balance: {}", 
                    amount, iban, newBalance);
            return true;
            
        } catch (Exception e) {
            log.error("Error during compensation operation for account: {}", iban, e);
            log.error("CRITICAL: Manual intervention required to credit {} to account {}", amount, iban);
            // Bu noktada bir notification service veya alert mekanizması ile yöneticilere bildirim gönderilebilir
            return false;
        }
    }
} 