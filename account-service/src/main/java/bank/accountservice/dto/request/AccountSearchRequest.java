package bank.accountservice.dto.request;

import bank.accountservice.entity.AccountStatus;
import bank.accountservice.entity.AccountType;
import bank.accountservice.entity.CurrencyType;
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