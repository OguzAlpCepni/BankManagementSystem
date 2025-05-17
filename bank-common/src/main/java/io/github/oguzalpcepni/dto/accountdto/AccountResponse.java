package io.github.oguzalpcepni.dto.accountdto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AccountResponse {
    private UUID id;
    private String iban;
    private String type;
    private String status;
    private BigDecimal balance;
    private BigDecimal overdraftLimit;
    private String currency;
    private Long customerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String accountDetails; // This will hold identity details or corporate details based on account type
} 