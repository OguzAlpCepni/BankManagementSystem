package bank.accountservice.service.Impl;


import bank.accountservice.entity.AccountStatus;
import bank.accountservice.entity.AccountType;
import bank.accountservice.entity.CorporateAccount;
import bank.accountservice.entity.CurrencyType;
import bank.accountservice.repository.CorporateAccountRepository;
import bank.accountservice.service.CorporateAccountService;
import bank.accountservice.validation.CorporateAccountBusinessRuleValidator;
import io.github.oguzalpcepni.dto.accountdto.CorporateAccountDto;
import io.github.oguzalpcepni.dto.accountdto.CorporateAccountResponse;
import io.github.oguzalpcepni.dto.accountdto.CreateCorporateAccountRequest;
import io.github.oguzalpcepni.dto.accountdto.TransactionResponse;
import io.github.oguzalpcepni.exceptions.type.BusinessException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Kurumsal hesap işlemlerini yönetir.
 * Bu servis sınıfı, kurumsal müşterilerin hesaplarıyla ilgili tüm işlemleri gerçekleştirir.
 * Hesap oluşturma, para yatırma/çekme, hesap bilgilerini güncelleme ve sorgulama gibi işlemler bu sınıf üzerinden yapılır.
 */
@Service
@RequiredArgsConstructor
public class CorporateAccountServiceImpl implements CorporateAccountService {

    private final CorporateAccountRepository corporateAccountRepository;
    private final CorporateAccountBusinessRuleValidator validator;

    @Override
    @Transactional
    public CorporateAccountResponse createAccount(CreateCorporateAccountRequest request) {
        // İş kurallarını doğrula
        validator.validateCreateAccount(request);
        
        // Yeni hesap nesnesi oluştur
        CorporateAccount account = new CorporateAccount();
        
        // İstek nesnesinden gelen bilgileri hesaba aktar
        account.setIban(ibanGenerator());
        account.setType(AccountType.valueOf(request.getType()));
        account.setStatus(AccountStatus.ACTIVE); // Yeni hesaplar varsayılan olarak aktiftir
        account.setBalance(request.getInitialBalance());
        account.setOverdraftLimit(request.getOverdraftLimit());
        account.setCurrency(CurrencyType.valueOf(request.getCurrency()));
        account.setCustomerId(request.getCustomerId());
        account.setTaxNumber(request.getTaxNumber());
        account.setCompanyName(request.getCompanyName());
        account.setAuthorizedPerson(request.getAuthorizedPerson());
        
        // Hesabı veritabanına kaydet
        CorporateAccount savedAccount = corporateAccountRepository.save(account);
        
        // Kaydedilen hesabı yanıt nesnesine dönüştür ve döndür
        return convertToResponse(savedAccount);
    }

    @Override
    public CorporateAccountResponse getAccountById(UUID id) {
        CorporateAccount account = corporateAccountRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Account not found with id: " + id));
            
        return convertToResponse(account);
    }
    @Override
    public List<CorporateAccountResponse> getAllAccounts() {
        List<CorporateAccount> accounts = corporateAccountRepository.findAll();
        return accounts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CorporateAccountResponse> getAccountsByCustomerId(UUID customerId) {
        List<CorporateAccount> accounts = corporateAccountRepository.findByCustomerId(customerId);
        return accounts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CorporateAccountResponse getAccountByIban(String iban) {
        CorporateAccount account = corporateAccountRepository.findByIban(iban)
            .orElseThrow(() -> new EntityNotFoundException("Account not found with IBAN: " + iban));
            
        return convertToResponse(account);
    }
    @Override
    public CorporateAccountResponse getAccountByTaxNumber(String taxNumber) {
        CorporateAccount account = corporateAccountRepository.findByTaxNumber(taxNumber)
            .orElseThrow(() -> new EntityNotFoundException("Account not found with Tax Number: " + taxNumber));
            
        return convertToResponse(account);
    }
    /**
     * Kurumsal hesap bilgilerini günceller.
     * 
     * @param id Güncellenecek hesabın ID'si
     * @param accountDto Güncellenecek hesap bilgileri
     * @return Güncellenmiş hesabın yanıtı
     * @throws EntityNotFoundException Hesap bulunamazsa fırlatılır
     */
    @Override
    @Transactional
    public CorporateAccountResponse updateAccountDetails(UUID id, CorporateAccountDto accountDto) {
        // Hesabı veritabanından bul
        CorporateAccount existingAccount = corporateAccountRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Account not found with id: " + id));
            
        // Vergi numarası validasyonu
        if (accountDto.getTaxNumber() != null && !accountDto.getTaxNumber().matches("\\d{10}")) {
            throw new BusinessException("Tax number must be 10 digits");
        }
            
        // Sadece güncellenebilir alanları güncelle (null olmayan alanlar)
        if (accountDto.getTaxNumber() != null) {
            existingAccount.setTaxNumber(accountDto.getTaxNumber());
        }
        if (accountDto.getCompanyName() != null) {
            existingAccount.setCompanyName(accountDto.getCompanyName());
        }
        if (accountDto.getAuthorizedPerson() != null) {
            existingAccount.setAuthorizedPerson(accountDto.getAuthorizedPerson());
        }
        if (accountDto.getOverdraftLimit() != null) {
            // Overdraft limiti kontrolü
            BigDecimal maxOverdraftLimit = new BigDecimal("10000.00");
            if (accountDto.getOverdraftLimit().compareTo(maxOverdraftLimit) > 0) {
                throw new BusinessException("Overdraft limit cannot exceed " + maxOverdraftLimit);
            }
            existingAccount.setOverdraftLimit(accountDto.getOverdraftLimit());
        }
        
        // Güncellenmiş hesabı kaydet
        CorporateAccount updatedAccount = corporateAccountRepository.save(existingAccount);
        
        // Güncellenmiş hesabı yanıt nesnesine dönüştür ve döndür
        return convertToResponse(updatedAccount);
    }

    /**
     * Bir kurumsal hesaba para yatırma işlemi gerçekleştirir.
     * 
     * @param id Para yatırılacak hesabın ID'si
     * @param amount Yatırılacak miktar
     * @return İşlem sonucu
     * @throws EntityNotFoundException Hesap bulunamazsa fırlatılır
     * @throws IllegalArgumentException Miktar 0 veya negatifse fırlatılır
     * @throws IllegalStateException Hesap aktif değilse fırlatılır
     */
    @Override
    @Transactional
    public TransactionResponse depositAmount(UUID id, BigDecimal amount) {
        // Hesap bakiyesini al, hesap yoksa hata fırlat
        CorporateAccount account = corporateAccountRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Account not found with id: " + id));
        
        BigDecimal oldBalance = account.getBalance();
        
        try {
            // İş kurallarını doğrula
            validator.validateDeposit(account, amount);
            
            // Para yatırma işlemini gerçekleştir
            account = depositInternal(account, amount);
            
            // Başarılı işlem yanıtını oluştur
            TransactionResponse response = new TransactionResponse();
            response.setAccountId(account.getId());
            response.setIban(account.getIban());
            response.setTransactionType("DEPOSIT");
            response.setAmount(amount);
            response.setBalanceBefore(oldBalance);
            response.setBalanceAfter(account.getBalance());
            response.setTransactionTime(LocalDateTime.now());
            response.setStatus("SUCCESS");
            response.setMessage("Deposit completed successfully");
            return response;
        } catch (BusinessException e) {
            // Hata durumunda başarısız işlem yanıtını oluştur
            TransactionResponse response = new TransactionResponse();
            response.setAccountId(id);
            response.setTransactionType("DEPOSIT");
            response.setAmount(amount);
            response.setBalanceBefore(oldBalance);
            response.setTransactionTime(LocalDateTime.now());
            response.setStatus("FAILED");
            response.setMessage(e.getMessage());
            return response;
        }
    }
    
    /**
     * İç kullanım için para yatırma işlemi gerçekleştirir.
     * 
     * @param account Güncellenecek hesap
     * @param amount Yatırılacak miktar
     * @return Güncellenmiş hesap
     * @throws IllegalArgumentException Miktar 0 veya negatifse fırlatılır
     * @throws IllegalStateException Hesap aktif değilse fırlatılır
     */
    private CorporateAccount depositInternal(CorporateAccount account, BigDecimal amount) {
        // Yeni bakiyeyi hesapla ve güncelle
        BigDecimal newBalance = account.getBalance().add(amount);
        account.setBalance(newBalance);
        
        // Güncellenmiş hesabı kaydet ve döndür
        return corporateAccountRepository.save(account);
    }

    /**
     * Bir kurumsal hesaptan para çekme işlemi gerçekleştirir.
     * 
     * @param id Para çekilecek hesabın ID'si
     * @param amount Çekilecek miktar
     * @return İşlem sonucu
     * @throws EntityNotFoundException Hesap bulunamazsa fırlatılır
     * @throws IllegalArgumentException Miktar 0 veya negatifse veya yeterli bakiye yoksa fırlatılır
     * @throws IllegalStateException Hesap aktif değilse fırlatılır
     */
    @Override
    @Transactional
    public TransactionResponse withdrawAmount(UUID id, BigDecimal amount) {
        // Hesabı bul, yoksa hata fırlat
        CorporateAccount account = corporateAccountRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Account not found with id: " + id));
        
        BigDecimal oldBalance = account.getBalance();
        
        try {
            // İş kurallarını doğrula
            validator.validateWithdrawal(account, amount);
            
            // Para çekme işlemini gerçekleştir
            account = withdrawInternal(account, amount);
            
            // Başarılı işlem yanıtını oluştur
            TransactionResponse response = new TransactionResponse();
            response.setAccountId(account.getId());
            response.setIban(account.getIban());
            response.setTransactionType("WITHDRAW");
            response.setAmount(amount);
            response.setBalanceBefore(oldBalance);
            response.setBalanceAfter(account.getBalance());
            response.setTransactionTime(LocalDateTime.now());
            response.setStatus("SUCCESS");
            response.setMessage("Withdrawal completed successfully");
            return response;
        } catch (BusinessException e) {
            // Hata durumunda başarısız işlem yanıtını oluştur
            TransactionResponse response = new TransactionResponse();
            response.setAccountId(id);
            response.setTransactionType("WITHDRAW");
            response.setAmount(amount);
            response.setBalanceBefore(oldBalance);
            response.setTransactionTime(LocalDateTime.now());
            response.setStatus("FAILED");
            response.setMessage(e.getMessage());
            return response;
        }
    }
    
    /**
     * İç kullanım için para çekme işlemi gerçekleştirir.
     * 
     * @param account Güncellenecek hesap
     * @param amount Çekilecek miktar
     * @return Güncellenmiş hesap
     * @throws IllegalArgumentException Miktar 0 veya negatifse veya yeterli bakiye yoksa fırlatılır
     * @throws IllegalStateException Hesap aktif değilse fırlatılır
     */
    private CorporateAccount withdrawInternal(CorporateAccount account, BigDecimal amount) {
        // Miktar kontrolü
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Withdrawal amount must be positive");
        }
        
        // Yeni bakiyeyi hesapla ve güncelle
        BigDecimal newBalance = account.getBalance().subtract(amount);
        account.setBalance(newBalance);
        
        // Güncellenmiş hesabı kaydet ve döndür
        return corporateAccountRepository.save(account);
    }
    /**
     * Kurumsal hesabın durumunu günceller (aktif, bloke, kapalı vb.)
     * 
     * @param id Durumu değiştirilecek hesabın ID'si
     * @param status Yeni durum (ACTIVE, INACTIVE, BLOCKED, CLOSED)
     * @return Güncellenmiş hesabın yanıtı
     * @throws BusinessException Hesap bulunamazsa fırlatılır
     * @throws BusinessException Geçersiz hesap durumu belirtilirse fırlatılır
     */
    @Override
    @Transactional
    public CorporateAccountResponse updateAccountStatus(UUID id, String status) {
        // Hesabı bul
        CorporateAccount account = corporateAccountRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Account not found with id: " + id));
                
        try {
            // String olarak gelen durumu enum'a çevir
            AccountStatus newStatus = AccountStatus.valueOf(status.toUpperCase());
            
            // İş kurallarını doğrula
            validator.validateStatusUpdate(account, newStatus);
            
            // Hesabın durumunu güncelle
            account.setStatus(newStatus);
            // Hesabı kaydet
            corporateAccountRepository.save(account);
            
            // Güncellenmiş hesabı yanıt nesnesine dönüştür ve döndür
            return convertToResponse(account);
        } catch (BusinessException e) {
            throw new BusinessException("Invalid account status: " + status + ". Valid values are: ACTIVE, INACTIVE, BLOCKED, CLOSED");
        }
    }
    
    /**
     * Entity'yi DTO'ya dönüştüren yardımcı metot
     * 
     * @param account Dönüştürülecek hesap entity'si
     * @return Oluşturulan yanıt DTO'su
     */
    private CorporateAccountResponse convertToResponse(CorporateAccount account) {
        CorporateAccountResponse response = new CorporateAccountResponse();
        response.setId(account.getId());
        response.setIban(account.getIban());
        response.setType(String.valueOf(account.getType()));
        response.setStatus(String.valueOf(account.getStatus()));
        response.setBalance(account.getBalance());
        response.setOverdraftLimit(account.getOverdraftLimit());
        response.setCurrency(String.valueOf(account.getCurrency()));
        response.setCustomerId(account.getCustomerId());
        response.setCreatedAt(account.getCreatedAt());
        response.setUpdatedAt(account.getUpdatedAt());
        response.setTaxNumber(account.getTaxNumber());
        response.setCompanyName(account.getCompanyName());
        response.setAuthorizedPerson(account.getAuthorizedPerson());
        return response;
    }
    private String ibanGenerator(){
        String COUNTRY_CODE = "TR";
        int BBAN_LENGTH = 24;
        // 1. Rasgele 24 haneli BBAN oluştur
        String bban = RandomStringUtils.randomNumeric(BBAN_LENGTH);

        // 2. Geçici IBAN: BBAN + ülke kodu + '00' (kontrol basamağı placeholder)
        String tempIban = bban + toNumeric(COUNTRY_CODE) + "00";

        // 3. Mod97 işlemi
        int mod = new BigInteger(tempIban).mod(BigInteger.valueOf(97)).intValue();

        // 4. Kontrol basamağını hesapla
        int checkDigits = 98 - mod;
        String checkStr = String.format("%02d", checkDigits);

        // 5. Son IBAN
        return COUNTRY_CODE + checkStr + bban;
    }
    private String toNumeric(String alpha) {
        StringBuilder sb = new StringBuilder();
        for (char c : alpha.toCharArray()) {
            int val = Character.getNumericValue(c);
            sb.append(val);
        }
        return sb.toString();
    }
} 