package bank.accountservice.service.Impl;

import bank.accountservice.dto.IndividualAccountDto;
import bank.accountservice.dto.request.CreateIndividualAccountRequest;
import bank.accountservice.dto.response.IndividualAccountResponse;
import bank.accountservice.dto.response.TransactionResponse;
import bank.accountservice.entity.AccountStatus;
import bank.accountservice.entity.IndividualAccount;
import bank.accountservice.repository.IndividualAccountRepository;
import bank.accountservice.service.IndividualAccountService;
import bank.accountservice.validation.AccountBusinessRuleValidator;
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
 * Bireysel hesap işlemlerini yönetir.
 * Bu servis sınıfı, bireysel müşterilerin hesaplarıyla ilgili tüm işlemleri gerçekleştirir.
 * Hesap oluşturma, para yatırma/çekme, hesap bilgilerini güncelleme ve sorgulama gibi işlemler bu sınıf üzerinden yapılır.
 */
@Service
@RequiredArgsConstructor
public class IndividualAccountServiceImpl implements IndividualAccountService {

    private final IndividualAccountRepository individualAccountRepository;
    private final AccountBusinessRuleValidator validator;

    @Override
    @Transactional
    public IndividualAccountResponse createAccount(CreateIndividualAccountRequest request) {
        // İş kurallarını doğrula
        validator.validateCreateAccount(request);
        // customerId başka birisininkiyle aynı olamaz buraya yaz
        // Yeni hesap nesnesi oluştur
        IndividualAccount account = new IndividualAccount();

        // İstek nesnesinden gelen bilgileri hesaba aktar
        account.setIban(ibanGenerator());
        account.setType(request.getType());
        account.setStatus(AccountStatus.ACTIVE); // Yeni hesaplar varsayılan olarak aktiftir
        account.setBalance(request.getInitialBalance());
        account.setOverdraftLimit(request.getOverdraftLimit());
        account.setCurrency(request.getCurrency());
        account.setCustomerId(request.getCustomerId());
        account.setIdentityNumber(request.getIdentityNumber());
        account.setFirstName(request.getFirstName());
        account.setLastName(request.getLastName());
        
        // Hesabı veritabanına kaydet
        IndividualAccount savedAccount = individualAccountRepository.save(account);
        
        // Kaydedilen hesabı yanıt nesnesine dönüştür ve döndür
        return convertToResponse(savedAccount);
    }


    @Override
    public IndividualAccountResponse getAccountById(UUID id) {
        IndividualAccount account = individualAccountRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Account not found with id: " + id));
        return convertToResponse(account);
    }

    @Override
    public List<IndividualAccountResponse> getAllAccounts() {
        List<IndividualAccount> accounts = individualAccountRepository.findAll();
        return accounts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Belirli bir müşteriye ait tüm hesapları listeler.
     * 
     * @param customerId Müşteri ID'si
     * @return Müşteriye ait hesapların listesi
     */
    @Override
    public List<IndividualAccountResponse> getAccountsByCustomerId(UUID customerId) {
        List<IndividualAccount> accounts = individualAccountRepository.findByCustomerId(customerId);
        return accounts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public IndividualAccountResponse getAccountByIban(String iban) {
        IndividualAccount account = individualAccountRepository.findByIban(iban)
            .orElseThrow(() -> new BusinessException("Account not found with IBAN: " + iban));
            
        return convertToResponse(account);
    }

    /**
     * Hesap bilgilerini günceller.
     * 
     * @param id Güncellenecek hesabın ID'si
     * @param accountDto Güncellenecek hesap bilgileri
     * @return Güncellenmiş hesabın yanıtı
     * @throws EntityNotFoundException Hesap bulunamazsa fırlatılır
     */
    @Override
    @Transactional
    public IndividualAccountResponse updateAccountDetails(UUID id, IndividualAccountDto accountDto) {
        // Hesabı veritabanından bul
        IndividualAccount existingAccount = individualAccountRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Account not found with id: " + id));
            
        // Sadece güncellenebilir alanları güncelle (null olmayan alanlar)
        if (accountDto.getIdentityNumber() != null) {
            existingAccount.setIdentityNumber(accountDto.getIdentityNumber());
        }
        if (accountDto.getFirstName() != null) {
            existingAccount.setFirstName(accountDto.getFirstName());
        }
        if (accountDto.getLastName() != null) {
            existingAccount.setLastName(accountDto.getLastName());
        }
        if (accountDto.getOverdraftLimit() != null) {
            existingAccount.setOverdraftLimit(accountDto.getOverdraftLimit());
        }
        
        // Güncellenmiş hesabı kaydet
        IndividualAccount updatedAccount = individualAccountRepository.save(existingAccount);
        
        // Güncellenmiş hesabı yanıt nesnesine dönüştür ve döndür
        return convertToResponse(updatedAccount);
    }

    /**
     * Bir hesaba para yatırma işlemi gerçekleştirir.
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
        BigDecimal oldBalance = individualAccountRepository.findById(id)
                .map(IndividualAccount::getBalance)
                .orElseThrow(() -> new BusinessException("Account not found with id: " + id));
        
        try {
            // Para yatırma işlemini gerçekleştir
            IndividualAccount account = depositInternal(id, amount);
            
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
        } catch (Exception e) {
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
     * @param id Para yatırılacak hesabın ID'si
     * @param amount Yatırılacak miktar
     * @return Güncellenmiş hesap
     * @throws EntityNotFoundException Hesap bulunamazsa fırlatılır
     * @throws IllegalArgumentException Miktar 0 veya negatifse fırlatılır
     * @throws IllegalStateException Hesap aktif değilse fırlatılır
     */
    private IndividualAccount depositInternal(UUID id, BigDecimal amount) {
        // Miktar kontrolü
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Deposit amount must be positive");
        }
        
        // Hesabı bul
        IndividualAccount account = individualAccountRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Account not found with id: " + id));
        
        // Hesap durumu kontrolü
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("Cannot deposit to an account with status: " + account.getStatus());
        }
                
        // Yeni bakiyeyi hesapla ve güncelle
        BigDecimal newBalance = account.getBalance().add(amount);
        account.setBalance(newBalance);
        
        // Güncellenmiş hesabı kaydet ve döndür
        return individualAccountRepository.save(account);
    }

    /**
     * Bir hesaptan para çekme işlemi gerçekleştirir.
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
        // Hesap bakiyesini al, hesap yoksa hata fırlat
        BigDecimal oldBalance = individualAccountRepository.findById(id)
                .map(IndividualAccount::getBalance)
                .orElseThrow(() -> new BusinessException("Account not found with id: " + id));
        
        try {
            // Para çekme işlemini gerçekleştir
            IndividualAccount account = withdrawInternal(id, amount);
            
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
     * @param id Para çekilecek hesabın ID'si
     * @param amount Çekilecek miktar
     * @return Güncellenmiş hesap
     * @throws EntityNotFoundException Hesap bulunamazsa fırlatılır
     * @throws IllegalArgumentException Miktar 0 veya negatifse veya yeterli bakiye yoksa fırlatılır
     * @throws IllegalStateException Hesap aktif değilse fırlatılır
     */
    private IndividualAccount withdrawInternal(UUID id, BigDecimal amount) {
        // Miktar kontrolü
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Withdrawal amount must be positive");
        }
        
        // Hesabı bul
        IndividualAccount account = individualAccountRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Account not found with id: " + id));
        
        // İş kurallarını doğrula
        validator.validateWithdrawal(account, amount);
                
        // Yeni bakiyeyi hesapla ve güncelle
        BigDecimal newBalance = account.getBalance().subtract(amount);
        account.setBalance(newBalance);
        
        // Güncellenmiş hesabı kaydet ve döndür
        return individualAccountRepository.save(account);
    }

    /**
     * Hesabın durumunu günceller (aktif, bloke, kapalı vb.)
     * 
     * @param id Durumu değiştirilecek hesabın ID'si
     * @param status Yeni durum (ACTIVE, INACTIVE, BLOCKED, CLOSED)
     * @return Güncellenmiş hesabın yanıtı
     * @throws EntityNotFoundException Hesap bulunamazsa fırlatılır
     * @throws IllegalArgumentException Geçersiz hesap durumu belirtilirse fırlatılır
     */
    @Override
    @Transactional
    public IndividualAccountResponse updateAccountStatus(UUID id, String status) {
        // Hesabı bul
        IndividualAccount account = individualAccountRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Account not found with id: " + id));
                
        try {
            // String olarak gelen durumu enum'a çevir
            AccountStatus newStatus = AccountStatus.valueOf(status.toUpperCase());
            
            // İş kurallarını doğrula
            validator.validateStatusUpdate(account, newStatus);
            
            // Hesabın durumunu güncelle
            account.setStatus(newStatus);
            // Hesabı kaydet
            individualAccountRepository.save(account);
            
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
    private IndividualAccountResponse convertToResponse(IndividualAccount account) {
        IndividualAccountResponse response = new IndividualAccountResponse();
        response.setId(account.getId());
        response.setIban(account.getIban());
        response.setType(account.getType());
        response.setStatus(account.getStatus());
        response.setBalance(account.getBalance());
        response.setOverdraftLimit(account.getOverdraftLimit());
        response.setCurrency(account.getCurrency());
        response.setCustomerId(account.getCustomerId());
        response.setCreatedAt(account.getCreatedAt());
        response.setUpdatedAt(account.getUpdatedAt());
        response.setIdentityNumber(account.getIdentityNumber());
        response.setFirstName(account.getFirstName());
        response.setLastName(account.getLastName());
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