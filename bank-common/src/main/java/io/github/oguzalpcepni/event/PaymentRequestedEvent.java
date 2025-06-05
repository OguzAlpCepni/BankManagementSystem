package io.github.oguzalpcepni.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PaymentRequestedEvent {
    private UUID paymentId;
    private UUID userId;
    private UUID accountId;
    private String billType;
    private String billerCode;
    private String subscriberNumber;
    private BigDecimal amount;
    private BigDecimal billAmount;
    private BigDecimal commissionAmount;
    private String paymentMethod;
    private LocalDateTime dueDate;
    private LocalDateTime requestedAt;
}
