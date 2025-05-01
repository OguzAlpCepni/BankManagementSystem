package bank.transferservice.entity;

/**
 * Transfer işleminin durumunu belirtir.
 */
public enum TransferStatus {
    PENDING,    // İşlem başlatıldı, henüz tamamlanmadı
    COMPLETED,  // İşlem başarıyla tamamlandı
    FAILED,     // İşlem başarısız oldu
    CANCELLED,  // İşlem iptal edildi
    REFUNDED    // İşlem iade edildi
} 