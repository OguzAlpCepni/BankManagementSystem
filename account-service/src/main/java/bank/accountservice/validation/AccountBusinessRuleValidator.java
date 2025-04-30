package bank.accountservice.validation;

import bank.accountservice.dto.request.CreateIndividualAccountRequest;
import bank.accountservice.entity.AccountStatus;
import bank.accountservice.entity.IndividualAccount;
import bank.accountservice.exception.BusinessRuleException;
import bank.accountservice.repository.IndividualAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Hesap işlemlerine ait iş kurallarını doğrulayan sınıf.
 * Servis sınıflarından bağımsız olarak iş kuralı validasyonları bu sınıfta yapılır.
 */
@Component
@RequiredArgsConstructor
public class AccountBusinessRuleValidator {

    private final IndividualAccountRepository individualAccountRepository;
    
    // Business Rules için sabitler
    private static final BigDecimal MINIMUM_INITIAL_BALANCE = new BigDecimal("100.00");
    private static final BigDecimal MAXIMUM_DAILY_WITHDRAWAL = new BigDecimal("5000.00");
    private static final BigDecimal MAXIMUM_OVERDRAFT_LIMIT = new BigDecimal("1000.00");
    private static final int MAXIMUM_ACCOUNTS_PER_CUSTOMER = 5;

    /**
     * Yeni hesap oluşturma iş kurallarını doğrular
     * 
     * @param request Hesap oluşturma isteği
     * @throws BusinessRuleException İş kuralı ihlali varsa fırlatılır
     */
    public void validateCreateAccount(CreateIndividualAccountRequest request) {
        // Minimum başlangıç bakiyesi kontrolü
        if (request.getInitialBalance().compareTo(MINIMUM_INITIAL_BALANCE) < 0) {
            throw new BusinessRuleException("Initial balance must be at least " + MINIMUM_INITIAL_BALANCE);
        }
        
        // Bir müşterinin maksimum hesap sayısı kontrolü
        long customerAccountCount = individualAccountRepository.countByCustomerId(request.getCustomerId());
        if (customerAccountCount >= MAXIMUM_ACCOUNTS_PER_CUSTOMER) {
            throw new BusinessRuleException("Customer cannot have more than " + MAXIMUM_ACCOUNTS_PER_CUSTOMER + " accounts");
        }
        
        // Overdraft limiti kontrolü
        if (request.getOverdraftLimit() != null && 
            request.getOverdraftLimit().compareTo(MAXIMUM_OVERDRAFT_LIMIT) > 0) {
            throw new BusinessRuleException("Overdraft limit cannot exceed " + MAXIMUM_OVERDRAFT_LIMIT);
        }
    }
    
    /**
     * Para çekme işlemi için iş kurallarını doğrular
     * 
     * @param account İşlem yapılacak hesap
     * @param amount Çekilecek tutar
     * @throws BusinessRuleException İş kuralı ihlali varsa fırlatılır
     */
    public void validateWithdrawal(IndividualAccount account, BigDecimal amount) {
        // Günlük maksimum para çekme limiti kontrolü
        if (amount.compareTo(MAXIMUM_DAILY_WITHDRAWAL) > 0) {
            throw new BusinessRuleException("Withdrawal amount exceeds the daily limit of " + MAXIMUM_DAILY_WITHDRAWAL);
        }
        
        // Hesap durumu kontrolü
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessRuleException("Cannot withdraw from an account with status: " + account.getStatus());
        }
        
        // Minimum bakiye kontrolü
        BigDecimal minimumBalance = BigDecimal.ZERO;
        BigDecimal overdraftLimit = account.getOverdraftLimit() != null ? account.getOverdraftLimit() : BigDecimal.ZERO;
        
        if (account.getBalance().subtract(amount).compareTo(minimumBalance.subtract(overdraftLimit)) < 0) {
            throw new BusinessRuleException("This transaction would exceed your overdraft limit");
        }
        
        // Yeterli bakiye kontrolü
        BigDecimal withdrawalLimit = account.getBalance().add(overdraftLimit);
        if (amount.compareTo(withdrawalLimit) > 0) {
            throw new BusinessRuleException("Insufficient funds for withdrawal");
        }
    }
    
    /**
     * Hesap durumu güncelleme için iş kurallarını doğrular
     * 
     * @param account İşlem yapılacak hesap
     * @param newStatus Yeni hesap durumu
     * @throws BusinessRuleException İş kuralı ihlali varsa fırlatılır
     */
    public void validateStatusUpdate(IndividualAccount account, AccountStatus newStatus) {
        // Kapatılacak bir hesabın bakiyesi sıfır olmalıdır
        if (newStatus == AccountStatus.CLOSED && account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessRuleException("Account must have zero balance to be closed");
        }
    }
} 