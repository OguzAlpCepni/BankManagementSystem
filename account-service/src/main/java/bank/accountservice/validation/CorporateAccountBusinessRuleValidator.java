package bank.accountservice.validation;


import bank.accountservice.entity.AccountStatus;
import bank.accountservice.entity.CorporateAccount;
import bank.accountservice.repository.CorporateAccountRepository;
import io.github.oguzalpcepni.dto.accountdto.CreateCorporateAccountRequest;
import io.github.oguzalpcepni.exceptions.type.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Kurumsal hesap işlemlerine ait iş kurallarını doğrulayan sınıf.
 * Servis sınıflarından bağımsız olarak iş kuralı validasyonları bu sınıfta yapılır.
 */
@Component
@RequiredArgsConstructor
public class CorporateAccountBusinessRuleValidator {

    private final CorporateAccountRepository corporateAccountRepository;

    // Business Rules için sabitler
    private static final BigDecimal MINIMUM_INITIAL_BALANCE = new BigDecimal("1000.00");
    private static final BigDecimal MAXIMUM_DAILY_WITHDRAWAL = new BigDecimal("50000.00");
    private static final BigDecimal MAXIMUM_OVERDRAFT_LIMIT = new BigDecimal("10000.00");
    private static final int MAXIMUM_ACCOUNTS_PER_CUSTOMER = 3;

    /**
     * Yeni kurumsal hesap oluşturma iş kurallarını doğrular
     *
     * @param request Hesap oluşturma isteği
     * @throws BusinessException İş kuralı ihlali varsa fırlatılır
     */
    public void validateCreateAccount(CreateCorporateAccountRequest request) {
        // Minimum başlangıç bakiyesi kontrolü
        if (request.getInitialBalance().compareTo(MINIMUM_INITIAL_BALANCE) < 0) {
            throw new BusinessException("Initial balance for corporate accounts must be at least " + MINIMUM_INITIAL_BALANCE);
        }

        // Bir müşterinin maksimum hesap sayısı kontrolü
        long customerAccountCount = corporateAccountRepository.countByCustomerId(request.getCustomerId());
        if (customerAccountCount >= MAXIMUM_ACCOUNTS_PER_CUSTOMER) {
            throw new BusinessException("Corporate customer cannot have more than " + MAXIMUM_ACCOUNTS_PER_CUSTOMER + " accounts");
        }

        // Overdraft limiti kontrolü
        if (request.getOverdraftLimit() != null &&
            request.getOverdraftLimit().compareTo(MAXIMUM_OVERDRAFT_LIMIT) > 0) {
            throw new BusinessException("Overdraft limit for corporate accounts cannot exceed " + MAXIMUM_OVERDRAFT_LIMIT);
        }

        // Vergi numarası kontrolü - rakamlardan oluşmalı ve 10 karakter olmalı
        if (request.getTaxNumber() == null || !request.getTaxNumber().matches("\\d{10}")) {
            throw new BusinessException("Tax number must be 10 digits");
        }

        // Şirket adı kontrolü
        if (request.getCompanyName() == null || request.getCompanyName().trim().isEmpty()) {
            throw new BusinessException("Company name cannot be empty");
        }
    }
    
    /**
     * Para çekme işlemi için iş kurallarını doğrular
     * 
     * @param account İşlem yapılacak hesap
     * @param amount Çekilecek tutar
     * @throws BusinessRuleException İş kuralı ihlali varsa fırlatılır
     */
    public void validateWithdrawal(CorporateAccount account, BigDecimal amount) {
        // Günlük maksimum para çekme limiti kontrolü
        if (amount.compareTo(MAXIMUM_DAILY_WITHDRAWAL) > 0) {
            throw new BusinessException("Withdrawal amount exceeds the daily limit of " + MAXIMUM_DAILY_WITHDRAWAL);
        }
        
        // Hesap durumu kontrolü
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("Cannot withdraw from an account with status: " + account.getStatus());
        }
        
        // Minimum bakiye kontrolü
        BigDecimal minimumBalance = BigDecimal.ZERO;
        BigDecimal overdraftLimit = account.getOverdraftLimit() != null ? account.getOverdraftLimit() : BigDecimal.ZERO;
        
        if (account.getBalance().subtract(amount).compareTo(minimumBalance.subtract(overdraftLimit)) < 0) {
            throw new BusinessException("This transaction would exceed your overdraft limit");
        }
        
        // Yeterli bakiye kontrolü
        BigDecimal withdrawalLimit = account.getBalance().add(overdraftLimit);
        if (amount.compareTo(withdrawalLimit) > 0) {
            throw new BusinessException("Insufficient funds for withdrawal");
        }
    }
    
    /**
     * Para yatırma işlemi için iş kurallarını doğrular
     * 
     * @param account İşlem yapılacak hesap
     * @param amount Yatırılacak tutar
     * @throws BusinessException İş kuralı ihlali varsa fırlatılır
     */
    public void validateDeposit(CorporateAccount account, BigDecimal amount) {
        // Miktar kontrolü
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Deposit amount must be positive");
        }
        
        // Maksimum tek seferde para yatırma limiti (kurumsal hesaplar için daha yüksek)
        BigDecimal maxDepositAmount = new BigDecimal("1000000.00");
        if (amount.compareTo(maxDepositAmount) > 0) {
            throw new BusinessException("Single deposit cannot exceed " + maxDepositAmount);
        }
        
        // Hesap durumu kontrolü
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("Cannot deposit to an account with status: " + account.getStatus());
        }
    }
    
    /**
     * Hesap durumu güncelleme için iş kurallarını doğrular
     * 
     * @param account İşlem yapılacak hesap
     * @param newStatus Yeni hesap durumu
     * @throws BusinessException İş kuralı ihlali varsa fırlatılır
     */
    public void validateStatusUpdate(CorporateAccount account, AccountStatus newStatus) {
        // Kapatılacak bir hesabın bakiyesi sıfır olmalıdır
        if (newStatus == AccountStatus.CLOSED && account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessException("Account must have zero balance to be closed");
        }
        
        // Blokeleme durumu için özel kontroller
        if (newStatus == AccountStatus.BLOCKED && account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessException("Cannot block an already closed account");
        }
    }
} 