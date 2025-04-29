package bank.accountservice.dto.response;

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

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class IndividualAccountResponse {
    private UUID id;
    private String iban;
    private AccountType type;
    private AccountStatus status;
    private BigDecimal balance;
    private BigDecimal overdraftLimit;
    private CurrencyType currency;
    private Long customerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Individual account specific fields
    private String identityNumber;
    private String firstName;
    private String lastName;
} 