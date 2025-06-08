package bank.transactionservice.service.impl;

import bank.transactionservice.client.AccountClient;
import bank.transactionservice.client.PaymentClient;
import bank.transactionservice.kafka.KafkaPaymentProducer;
import bank.transactionservice.service.PaymentService;
import io.github.oguzalpcepni.dto.accountdto.AccountDto;
import io.github.oguzalpcepni.dto.accountdto.CreditRequest;
import io.github.oguzalpcepni.event.PaymentRequestedEvent;
import io.github.oguzalpcepni.exceptions.type.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final AccountClient accountClient;
    private final KafkaPaymentProducer kafkaPaymentProducer;
    private final PaymentClient paymentClient;
    @Override
    public void handlePaymentRequested(PaymentRequestedEvent paymentRequestedEvent) {
        UUID paymentId = paymentRequestedEvent.getPaymentId();
        UUID accountId = paymentRequestedEvent.getAccountId();
        BigDecimal amount = paymentRequestedEvent.getAmount();
        String billerResponse = null;
        String errorMessage = null;

        log.info("[TransactionService] Received PaymentRequestedEvent for paymentId={}", paymentId);

        ResponseEntity<Boolean> reservationOk;
        // 1. Hesaptan para çek
        try {
            reservationOk = accountClient.debitAccount(accountId,amount,paymentRequestedEvent.getPaymentMethod(),paymentId);
            if(reservationOk.getBody() != null && !reservationOk.getBody()){
                errorMessage = "account does not have money enought";
                log.warn("[TransactionService] Reservation failed for paymentId={}", paymentId);
                kafkaPaymentProducer.publishStatusUpdate(paymentId, "FAILED", null, errorMessage);
            }
            log.info("[TransactionService] Reservation succeeded for paymentId={}", paymentId);
        }catch (BusinessException ex) {
            errorMessage = "Exception during reserve: " + ex.getMessage();
            log.error("[TransactionService] Exception on reserve for paymentId={}: {}", paymentId, ex.getMessage());
            kafkaPaymentProducer.publishStatusUpdate(paymentId, "FAILED", null, errorMessage);
        }


        boolean isBillingSuccessful;
        try {
            Map<String,Object> req = Map.of(
                    "billerCode", paymentRequestedEvent.getBillerCode(),
                    "subscriberNumber", paymentRequestedEvent.getSubscriberNumber(),
                    "amount", amount
            );
            ResponseEntity<Map<String,Object>> resp = paymentClient.pay(req);
            isBillingSuccessful = resp.getStatusCode().is2xxSuccessful()
                    && Boolean.TRUE.equals(Objects.requireNonNull(resp.getBody()).get("success"));
            if (isBillingSuccessful) {
                // Success logic here
                billerResponse = resp.getBody().get("paymentId").toString(); // buraya aslında transactionId yazılması gerek todo
                log.info("[TransactionService] Billing succeeded for paymentId={}, txId={}", paymentId, billerResponse);
            }else {
                errorMessage = "Billing provider returned failure";
                log.warn("[TransactionService] Billing failed for paymentId={}", paymentId);
            }
        }catch (BusinessException businessException){
            isBillingSuccessful = false;
            errorMessage = "Exception during billing: " + businessException.getMessage();
            log.error("[TransactionService] Exception on billing for paymentId={}: {}", paymentId, businessException.getMessage());
        }

        // 3️⃣ Commit / Rollback
        if(isBillingSuccessful){
            try {
                log.info("[TransactionService] Billing request succeeded for paymentId={}", paymentId);
                kafkaPaymentProducer.publishStatusUpdate(paymentId,"COMPLETED",billerResponse,errorMessage);
            }catch (BusinessException businessException){
                errorMessage = "debit failed: " + businessException.getMessage();
                log.error("[TransactionService] Exception on billing for paymentId={}", paymentId);
                ResponseEntity<Optional<AccountDto>> account = accountClient.getAccountById(paymentRequestedEvent.getAccountId());
                CreditRequest creditRequest = new CreditRequest(amount, Objects.requireNonNull(account.getBody()).get().getCurrency(),paymentId,paymentRequestedEvent.getPaymentMethod());
                accountClient.compensateDebit(account.getBody().get().getIban(),creditRequest);
                kafkaPaymentProducer.publishStatusUpdate(paymentId, "FAILED", null, errorMessage);
            }
        }
        else {
            ResponseEntity<Optional<AccountDto>> account = accountClient.getAccountById(paymentRequestedEvent.getAccountId());
            CreditRequest creditRequest = new CreditRequest(amount, Objects.requireNonNull(account.getBody()).get().getCurrency(),paymentId,paymentRequestedEvent.getPaymentMethod());
            accountClient.compensateDebit(account.getBody().get().getIban(),creditRequest);
            kafkaPaymentProducer.publishStatusUpdate(paymentId, "FAILED", null, errorMessage);
        }
    }
}
