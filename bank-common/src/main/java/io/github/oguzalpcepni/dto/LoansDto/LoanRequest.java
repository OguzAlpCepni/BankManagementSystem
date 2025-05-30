package io.github.oguzalpcepni.dto.LoansDto;

import java.math.BigDecimal;
import java.util.UUID;

public class LoanRequest {
    /** Müşteri UUID'si */
    private UUID customerId;

    /** Kredi tutarı */
    private BigDecimal amount;

    /** Taksit sayısı */
    private Integer installmentCount;

    /** Kredi amacı */
    private String purpose;


}
