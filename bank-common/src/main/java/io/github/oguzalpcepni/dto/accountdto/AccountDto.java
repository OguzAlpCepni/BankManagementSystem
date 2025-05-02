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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    private UUID id;
    private String iban;
    private AccountType type;
    private AccountStatus status;
    private BigDecimal balance;
    private BigDecimal overdraftLimit;
    private CurrencyType currency;
    private Long customerId;
    private int version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 