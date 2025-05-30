package io.github.oguzalpcepni.event;

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
public class LoanApplicationCreatedEvent {

    UUID loanId;
    UUID customerId;
    BigDecimal amount;
    int installmentCount;
    String purpose;


}
