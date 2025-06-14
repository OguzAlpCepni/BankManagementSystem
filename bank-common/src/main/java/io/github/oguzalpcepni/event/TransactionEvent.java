package io.github.oguzalpcepni.event;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEvent {
    private UUID transactionId;
    private UUID transferId;
    private UUID sourceAccountId;
    private UUID targetAccountId;
    private String sourceIban;
    private String targetIban;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String type;
    private String status;
    private LocalDateTime timestamp;
    private String eventType; // CREATED, COMPLETED, FAILED
}
