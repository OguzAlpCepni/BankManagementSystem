package io.github.oguzalpcepni.dto.LoansDto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanTransferResponse {
    private UUID loanId;
    private String newStatus;        // APPROVED veya TRANSFER_FAILED
    private String transferStatus;       // “COMPLETED” veya hata mesajı
    private String message;              // İsteğe bağlı ek bir mesaj
}
