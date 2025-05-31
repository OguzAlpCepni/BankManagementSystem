package io.github.oguzalpcepni.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FraudCheckEvent {


    private UUID loanId;
    private Integer creditScore;
    private BigDecimal amount;
}
