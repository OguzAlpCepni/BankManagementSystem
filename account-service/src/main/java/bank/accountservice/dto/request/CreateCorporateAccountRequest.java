package bank.accountservice.dto.request;

import bank.accountservice.entity.AccountType;
import bank.accountservice.entity.CurrencyType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateCorporateAccountRequest {

    @NotNull(message = "Account type is required")
    private AccountType type;
    
    @NotNull(message = "Currency type is required")
    private CurrencyType currency;
    
    @NotNull(message = "Initial balance is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Initial balance cannot be negative")
    @Digits(integer = 12, fraction = 2, message = "Invalid balance format")
    private BigDecimal initialBalance;
    
    private BigDecimal overdraftLimit;
    
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    @NotBlank(message = "Tax number is required")
    private String taxNumber;
    
    @NotBlank(message = "Company name is required")
    private String companyName;
    
    @NotBlank(message = "Authorized person is required")
    private String authorizedPerson;
} 