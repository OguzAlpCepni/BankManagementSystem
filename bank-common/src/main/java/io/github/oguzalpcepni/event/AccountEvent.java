package io.github.oguzalpcepni.event;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountEvent {
    private UUID accountId;
    private UUID transactionId;
    private String iban;
    private BigDecimal amount;
    private String currency;
    private String eventType; // DEBITED, CREDITED, DEBIT_FAILED, CREDIT_FAILED
    private LocalDateTime timestamp;
    private String errorMessage;
}
