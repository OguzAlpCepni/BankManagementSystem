package bank.accountservice.dto;

import bank.accountservice.entity.AccountStatus;
import bank.accountservice.entity.AccountType;
import bank.accountservice.entity.CurrencyType;
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