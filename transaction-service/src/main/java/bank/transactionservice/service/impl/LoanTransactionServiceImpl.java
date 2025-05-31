package bank.transactionservice.service.impl;

import bank.transactionservice.client.LoanClient;
import bank.transactionservice.entity.LoanTransaction;
import bank.transactionservice.entity.TransactionStatus;
import bank.transactionservice.kafka.KafkaLoanProducerImpl;
import bank.transactionservice.kafka.KafkaLoanProducerService;
import bank.transactionservice.repository.LoanTransactionRepository;
import bank.transactionservice.service.LoanTransactionService;
import io.github.oguzalpcepni.event.LoanApplicationCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class LoanTransactionServiceImpl implements LoanTransactionService {


    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanClient loanClient;
    private final KafkaLoanProducerService kafkaLoanProducerService;
    private final KafkaLoanProducerImpl kafkaLoanProducerImpl;

    @Override
    @Transactional
    public void createLoanTransaction(LoanApplicationCreatedEvent loanApplicationCreatedEvent) {
        log.info("start loan transaction");
        log.info("loan application converting loan transaction");

        ResponseEntity<Integer> creditScore = loanClient.getCreditScore(loanApplicationCreatedEvent.getCustomerId());
        if(creditScore == null) {
            throw new RuntimeException("credit score not found");
        }


        LoanTransaction loanTransaction = new LoanTransaction();
        loanTransaction.setLoanId(loanApplicationCreatedEvent.getLoanId());
        loanTransaction.setCustomerId(loanApplicationCreatedEvent.getCustomerId());
        loanTransaction.setAmount(loanApplicationCreatedEvent.getAmount());
        loanTransaction.setInstallmentCount(loanApplicationCreatedEvent.getInstallmentCount());
        loanTransaction.setCreditScore(creditScore.getBody());
        loanTransaction.setFraudCheckPassed(null);
        loanTransaction.setStatus(TransactionStatus.PENDING);
        loanTransactionRepository.save(loanTransaction);

        // burada eventimiz fraud check işlemine gönderdik
        kafkaLoanProducerImpl.sendFraudCheckEvent(loanTransaction);
    }
}
