package bank.paymentservice.entity;

public enum PaymentMethod {
    ACCOUNT_BALANCE("Hesap Bakiyesi"),
    MOBILE_PAYMENT("Mobil Ödeme");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
