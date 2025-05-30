package io.github.oguzalpcepni.dto.LoansDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoanStatusResponse {
    private UUID id;
    private UUID externalLoanId;
    private String status;      // Örneğin PENDING
    private LocalDateTime createdAt;
}
