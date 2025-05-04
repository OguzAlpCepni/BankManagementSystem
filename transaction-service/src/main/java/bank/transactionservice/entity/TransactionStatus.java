package bank.transactionservice.entity;

public enum TransactionStatus {
    PENDING,
    IN_PROGRESS,
    SOURCE_ACCOUNT_DEBITED,
    TARGET_ACCOUNT_CREDITED,
    COMPLETED,
    FAILED,
    CANCELLED
} 