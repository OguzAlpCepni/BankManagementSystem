package io.github.oguzalpcepni.dto.LoansDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class LoanResponse {
    /** Kredi başvuru UUID'si (DB PK) */
    private UUID id;

    /** Dışa gösterilecek kredi referans numarası */
    private UUID externalLoanId;

    /** Müşteri UUID'si */
    private UUID customerId;

    /** Kredi tutarı */
    private BigDecimal amount;

    /** Taksit sayısı */
    private Integer installmentCount;

    /** Kredi amacı */
    private String purpose;

    /** Kredi durumu */
    private String status;

    /** Faiz oranı */
    private BigDecimal interestRate;

    /** Oluşturulma zamanı */
    private LocalDateTime createdAt;

    /** Güncellenme zamanı */
    private LocalDateTime updatedAt;
}
