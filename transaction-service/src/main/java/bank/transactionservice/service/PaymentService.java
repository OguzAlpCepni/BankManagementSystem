package bank.transactionservice.service;

import io.github.oguzalpcepni.event.PaymentRequestedEvent;

public interface PaymentService {

    public void handlePaymentRequested(PaymentRequestedEvent paymentRequestedEvent);
}
