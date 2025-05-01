package bank.accountservice.dto.request;

import bank.accountservice.entity.AccountType;
import bank.accountservice.entity.CurrencyType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateIndividualAccountRequest {
    
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
    private UUID customerId;
    
    @NotBlank(message = "Identity number is required")
    private String identityNumber;
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
}
