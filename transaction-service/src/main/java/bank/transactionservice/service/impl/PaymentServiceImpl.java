package bank.transactionservice.service.impl;

import bank.transactionservice.service.PaymentService;
import io.github.oguzalpcepni.event.PaymentRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {


    @Override
    public void handlePaymentRequested(PaymentRequestedEvent paymentRequestedEvent) {
        UUID paymentId = paymentRequestedEvent.getPaymentId();
        UUID accountId = paymentRequestedEvent.getAccountId();
        Double amount = paymentRequestedEvent.getAmount().doubleValue();
        String billerResponse = null;
        String errorMessage = null;

        log.info("[TransactionService] Received PaymentRequestedEvent for paymentId={}", paymentId);

        // 1. Hesaptan para rezerve et (reserve)
        try {

        }

    }
}
