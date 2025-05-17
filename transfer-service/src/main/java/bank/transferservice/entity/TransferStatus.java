package bank.transferservice.entity;

/**
 * Transfer işleminin durumunu belirtir.
 */
public enum TransferStatus {
    PENDING,    // İşlem başlatıldı, henüz tamamlanmadı
    DEBITED,    // Kaynak hesaptan para çekildi, hedef hesaba henüz yatırılmadı
    COMPLETED,  // İşlem başarıyla tamamlandı
    FAILED,     // İşlem başarısız oldu
    CANCELLED,  // İşlem iptal edildi
    REFUNDED    // İşlem iade edildi
} 