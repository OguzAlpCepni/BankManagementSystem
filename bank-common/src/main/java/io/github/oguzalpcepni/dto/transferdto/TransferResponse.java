package io.github.oguzalpcepni.dto.transferdto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {
    private UUID id;
    private UUID sourceAccountId;
    private UUID targetAccountId;
    private String sourceIban;
    private String targetIban;
    private BigDecimal amount;
    private String currency;
    private String transferType;
    private String status;
    private String description;
    private String transactionReference;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 