package bank.transferservice.dto;

import bank.transferservice.entity.TransferType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {
    
    @NotNull(message = "Source account ID is required")
    private UUID sourceAccountId;
    
    @NotNull(message = "Target account ID is required")
    private UUID targetAccountId;
    
    @NotBlank(message = "Source IBAN is required")
    private String sourceIban;
    
    @NotBlank(message = "Target IBAN is required")
    private String targetIban;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    private String currency;
    
    @NotNull(message = "Transfer type is required")
    private TransferType transferType;
    
    private String description;
    
    private String transactionReference;
} 