package bank.transactionservice.service;

import io.github.oguzalpcepni.event.PaymentRequestedEvent;

public interface PaymentService {

    void handlePaymentRequested(PaymentRequestedEvent paymentRequestedEvent);
}
