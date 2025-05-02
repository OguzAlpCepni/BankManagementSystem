package io.github.oguzalpcepni.dto.accountdto;

import io.github.oguzalpcepni.dto.enums.AccountStatus;
import io.github.oguzalpcepni.dto.enums.AccountType;
import io.github.oguzalpcepni.dto.enums.CurrencyType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CorporateAccountResponse {
    private UUID id;
    private String iban;
    private AccountType type;
    private AccountStatus status;
    private BigDecimal balance;
    private BigDecimal overdraftLimit;
    private CurrencyType currency;
    private UUID customerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Corporate account specific fields
    private String taxNumber;
    private String companyName;
    private String authorizedPerson;
} 