package io.github.oguzalpcepni.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PaymentStatusUpdateEvent {
    // PaymentStatusUpdateEvent.java (Orchestrator’un gönderdiği event)
    private UUID paymentId;
    private String newStatus;        // COMPLETED, FAILED, vb.
    private String billerResponse;
    private String errorMessage;
}