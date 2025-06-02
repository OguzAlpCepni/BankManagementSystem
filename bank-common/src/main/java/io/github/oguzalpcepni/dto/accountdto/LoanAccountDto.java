package io.github.oguzalpcepni.dto.accountdto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class LoanAccountDto {

    @NotNull(message = "Target account ID is required")
    private UUID targetAccountId;
    @NotBlank(message = "Target IBAN is required")
    private String targetIban;

    //private BigDecimal amount;

    //private String currency;

    //private String transferType;
    //private String description;
    //private String transactionReference;
}
