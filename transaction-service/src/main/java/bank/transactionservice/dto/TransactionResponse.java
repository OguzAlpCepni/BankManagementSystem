package bank.transactionservice.dto;

import bank.transactionservice.entity.TransactionStatus;
import bank.transactionservice.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {
    private UUID id;
    private UUID sourceAccountId;
    private UUID targetAccountId;
    private String sourceIban;
    private String targetIban;
    private BigDecimal amount;
    private String currency;
    private String description;
    private TransactionType type;
    private TransactionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDateTime failedAt;
    private boolean sourceAccountDebited;
    private boolean targetAccountCredited;
    private String errorMessage;
} 