package bank.transactionservice.service.impl;

import bank.transactionservice.client.AccountClient;
import bank.transactionservice.entity.Transaction;
import bank.transactionservice.entity.TransactionStatus;
import bank.transactionservice.entity.TransactionType;
import bank.transactionservice.kafka.KafkaProducerService;
import bank.transactionservice.repository.TransactionRepository;
import bank.transactionservice.service.TransactionService;
import io.github.oguzalpcepni.dto.accountdto.CreditRequest;
import io.github.oguzalpcepni.event.TransferEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountClient accountClient;
    private final KafkaProducerService kafkaProducerService;

    @Override
    @Transactional
    public Transaction createTransaction(TransferEvent transferEvent) {
        log.info("Creating transaction from transfer event: {}", transferEvent);

        Transaction transaction = Transaction.builder()
                .transferId(transferEvent.getTransferId())
                .sourceAccountId(transferEvent.getSourceAccountId())
                .targetAccountId(transferEvent.getTargetAccountId())
                .sourceIban(transferEvent.getSourceIban())
                .targetIban(transferEvent.getTargetIban())
                .amount(transferEvent.getAmount())
                .currency(transferEvent.getCurrency())
                .description(transferEvent.getDescription())
                .type(TransactionType.INTERNAL_TRANSFER)
                .status(TransactionStatus.PENDING)
                .sourceAccountDebited(transferEvent.getSourceAccountDebited())
                .targetAccountCredited(false)
                .createdAt(LocalDateTime.now())
                .build();

        transaction = transactionRepository.save(transaction);
        
        // Sequence diagram'a uygun olarak credit işlemini başlat
        log.info("Initiating credit operation for transaction: {}", transaction.getId());
        try {
            CreditRequest creditRequest = new CreditRequest(
                    transaction.getAmount(),
                    transaction.getCurrency(),
                    transaction.getId(),
                    transaction.getDescription()
            );
            
            ResponseEntity<Boolean> creditResponse = accountClient.creditAccount(
                    transaction.getTargetIban(),
                    creditRequest
            );
            
            if (creditResponse.getBody() != null && creditResponse.getBody()) {
                // Credit successful
                log.info("Credit operation successful for transaction: {}", transaction.getId());
                transaction.setTargetAccountCredited(true);
                transaction.setStatus(TransactionStatus.COMPLETED);
                transaction.setCompletedAt(LocalDateTime.now());
                transaction = transactionRepository.save(transaction);
                
                // Send transaction completed event
                kafkaProducerService.sendTransactionCompletedEvent(transaction);
            } else {
                // Credit failed
                log.error("Credit operation failed for transaction: {}", transaction.getId());
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setFailedAt(LocalDateTime.now());
                transaction.setErrorMessage("Credit operation failed");
                transaction = transactionRepository.save(transaction);
                
                // Telafi edici işlem (compensating transaction): Kaynak hesaba parayı geri yatır
                compensateSourceAccount(transaction);
                
                // Send transaction failed event
                kafkaProducerService.sendTransactionFailedEvent(transaction);
            }
        } catch (Exception e) {
            log.error("Error during credit operation: {}", e.getMessage(), e);
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailedAt(LocalDateTime.now());
            transaction.setErrorMessage("Error during credit operation: " + e.getMessage());
            transaction = transactionRepository.save(transaction);
            
            // Telafi edici işlem (compensating transaction): Kaynak hesaba parayı geri yatır
            compensateSourceAccount(transaction);
            
            // Send transaction failed event
            kafkaProducerService.sendTransactionFailedEvent(transaction);
        }
        
        return transaction;
    }

    @Override
    public List<Transaction> findBySourceAccountId(UUID sourceAccountId) {
        return transactionRepository.findBySourceAccountId(sourceAccountId);
    }

    @Override
    public List<Transaction> findByTargetAccountId(UUID targetAccountId) {
        return transactionRepository.findByTargetAccountId(targetAccountId);
    }

    @Override
    public List<Transaction> findByStatus(TransactionStatus status) {
        return transactionRepository.findByStatus(status);
    }

    /**
     * Telafi edici işlem: Kaynak hesaba çekilen parayı geri yatır
     * Bu metod SAGA patterninde bir işlem başarısız olduğunda
     * önceki başarılı adımları geri almak için kullanılır
     */
    private void compensateSourceAccount(Transaction transaction) {
        log.info("Starting compensating transaction for failed credit operation, transaction ID: {}", transaction.getId());
        try {
            CreditRequest compensationRequest = new CreditRequest(
                    transaction.getAmount(),
                    transaction.getCurrency(),
                    transaction.getId(),
                    "Compensation for failed transfer: " + transaction.getDescription()
            );
            
            ResponseEntity<Boolean> compensationResponse = accountClient.compensateDebit(
                    transaction.getSourceIban(),
                    compensationRequest
            );
            
            if (compensationResponse.getBody() != null && compensationResponse.getBody()) {
                log.info("Compensation successful, funds returned to source account: {}", transaction.getSourceIban());
            } else {
                log.error("Compensation failed, manual intervention required for source account: {}", transaction.getSourceIban());
            }
        } catch (Exception e) {
            log.error("Error during compensation operation: {}", e.getMessage(), e);
            log.error("Manual intervention required to refund source account: {}", transaction.getSourceIban());
        }
    }
} 