package bank.accountservice.service;

import bank.accountservice.entity.Account;
import io.github.oguzalpcepni.dto.accountdto.AccountDto;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface AccountService {
    Optional<AccountDto> findAccountById(UUID id);
    Optional<AccountDto> findAccountByIban(String iban);
    
    void validateAccount(UUID id);
    void validateAccount(String iban);
    
    boolean hasEnoughBalance(UUID id, BigDecimal amount);
    
    /**
     * Hesaptan para çekme işlemi
     * @param id Hesap ID
     * @param amount Çekilecek miktar
     * @param description İşlem açıklaması
     * @param transactionId İşlem ID
     * @return İşlem başarılı ise true, değilse false
     */
    boolean debitAccount(UUID id, BigDecimal amount, String description, UUID transactionId);
    
    /**
     * Hesaba para yatırma işlemi
     * @param id Hesap ID
     * @param amount Yatırılacak miktar
     * @param description İşlem açıklaması
     * @param transactionId İşlem ID
     * @return İşlem başarılı ise true, değilse false
     */
    boolean creditAccount(String iban, BigDecimal amount, String description, UUID transactionId);
    
    /**
     * Saga pattern telafi işlemi - Başarısız bir işlem sonrasında kaynak hesaba
     * çekilen parayı geri yatırma işlemi
     * @param iban Hesap IBAN
     * @param amount Yatırılacak miktar
     * @param description İşlem açıklaması
     * @param transactionId İşlem ID
     * @return İşlem başarılı ise true, değilse false
     */
    boolean compensateDebit(String iban, BigDecimal amount, String description, UUID transactionId);
} 