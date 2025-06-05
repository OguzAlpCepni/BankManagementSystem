package io.github.oguzalpcepni.dto.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDto {

    @NotNull
    private UUID userId;

    @NotNull
    private UUID accountId;

    @NotNull
    private String billType;

    @NotBlank
    private String billerCode;

    @NotBlank
    private String subscriberNumber;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal billAmount;

    private BigDecimal commissionAmount; // Komisyon var ise

    @NotNull
    private String paymentMethod;

    private String billDescription;

    private LocalDateTime dueDate;

}
