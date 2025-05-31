package bank.transactionservice.entity;

public enum TransactionStatus {
    PENDING,
    IN_PROGRESS,
    SOURCE_ACCOUNT_DEBITED,
    TARGET_ACCOUNT_CREDITED,
    COMPLETED,
    FAILED,
    CANCELLED,
    UNDERWRITING_COMPLETED, // Kredi skoru/underwriting tamamlandı
    FRAUD_CHECK_PENDING,    // Fraud kontrolü bekleniyor
    FRAUD_CHECK_FAILED,     // Fraud kontrolü başarısız
    APPROVAL_COMMAND_SENT,  // Onay komutu Kafka ile gönderildi
    APPROVED,               // Kredi onaylandı
    REJECTED                // Kredi reddedildi
} 