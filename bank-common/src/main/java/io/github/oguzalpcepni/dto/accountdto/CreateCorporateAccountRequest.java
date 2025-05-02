package io.github.oguzalpcepni.dto.accountdto;


import io.github.oguzalpcepni.dto.enums.AccountType;
import io.github.oguzalpcepni.dto.enums.CurrencyType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    private UUID customerId;
    
    @NotBlank(message = "Tax number is required")
    private String taxNumber;
    
    @NotBlank(message = "Company name is required")
    private String companyName;
    
    @NotBlank(message = "Authorized person is required")
    private String authorizedPerson;
} 