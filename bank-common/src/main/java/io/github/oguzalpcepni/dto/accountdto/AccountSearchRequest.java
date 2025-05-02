package io.github.oguzalpcepni.dto.accountdto;

import io.github.oguzalpcepni.dto.enums.AccountStatus;
import io.github.oguzalpcepni.dto.enums.AccountType;
import io.github.oguzalpcepni.dto.enums.CurrencyType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AccountSearchRequest {
    private Long customerId;
    private String iban;
    private String identityNumber;
    private String taxNumber;
    private AccountType type;
    private AccountStatus status;
    private CurrencyType currency;
} 