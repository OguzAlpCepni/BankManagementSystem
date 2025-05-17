package io.github.oguzalpcepni.dto.accountdto;



import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    private UUID id;
    private String iban;
    private String type;
    private String status;
    private BigDecimal balance;
    private BigDecimal overdraftLimit;
    private String currency;
    private Long customerId;
    private int version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 