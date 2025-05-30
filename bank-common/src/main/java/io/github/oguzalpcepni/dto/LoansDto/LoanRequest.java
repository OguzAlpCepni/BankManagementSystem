package io.github.oguzalpcepni.dto.LoansDto;

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
public class LoanRequest {
    /** Müşteri UUID'si */
    private UUID customerId;

    /** Kredi tutarı */
    private BigDecimal amount;

    /** Taksit sayısı */
    private int installmentCount;

    /** Kredi amacı */
    private String purpose;


}
