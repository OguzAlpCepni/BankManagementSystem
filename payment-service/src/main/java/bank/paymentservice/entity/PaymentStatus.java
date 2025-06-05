package bank.paymentservice.entity;

public enum PaymentStatus {
    PENDING("Beklemede"),
    PROCESSING("İşleniyor"),
    COMPLETED("Tamamlandı"),
    FAILED("Başarısız"),
    CANCELLED("İptal Edildi"),
    REFUNDED("İade Edildi");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
