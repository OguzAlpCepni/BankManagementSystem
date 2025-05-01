package bank.transferservice.dto.response;

import bank.transferservice.entity.TransferStatus;
import bank.transferservice.entity.TransferType;
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
public class TransferResponse {
    private UUID id;
    private String sourceIban;
    private String targetIban;
    private BigDecimal amount;
    private String currency;
    private String description;
    private TransferType type;
    private TransferStatus status;
    private String transactionReference;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String errorMessage;
} 