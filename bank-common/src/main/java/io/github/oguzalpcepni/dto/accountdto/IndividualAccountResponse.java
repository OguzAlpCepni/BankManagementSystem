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
public class IndividualAccountResponse {
    private UUID id;
    private String iban;
    private String type;
    private String status;
    private BigDecimal balance;
    private BigDecimal overdraftLimit;
    private String currency;
    private UUID customerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Individual account specific fields
    private String identityNumber;
    private String firstName;
    private String lastName;
} 