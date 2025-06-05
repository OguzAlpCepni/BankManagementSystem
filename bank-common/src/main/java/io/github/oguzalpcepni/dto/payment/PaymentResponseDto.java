package io.github.oguzalpcepni.dto.payment;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PaymentResponseDto {
    private UUID id;
    private String paymentReference;
    private String paymentStatus;
    private BigDecimal amount;
    private BigDecimal billAmount;
    private BigDecimal commissionAmount;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String errorMessage;  // Başarısız olduysa
}
