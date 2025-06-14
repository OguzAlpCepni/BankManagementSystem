package io.github.oguzalpcepni.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PaymentStatusUpdateDTOo {

    private UUID paymentId;
    private String newStatus;
    private String billerResponse;
    private String errorMessage;
}
