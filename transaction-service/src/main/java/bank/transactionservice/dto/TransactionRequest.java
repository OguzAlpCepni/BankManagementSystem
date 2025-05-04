package bank.transactionservice.dto;

import bank.transactionservice.entity.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequest {
    
    @NotNull(message = "Source account ID is required")
    private UUID sourceAccountId;
    
    @NotNull(message = "Target account ID is required")
    private UUID targetAccountId;
    
    @NotBlank(message = "Source IBAN is required")
    private String sourceIban;
    
    @NotBlank(message = "Target IBAN is required")
    private String targetIban;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    private String currency;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotNull(message = "Transaction type is required")
    private TransactionType type;
} 