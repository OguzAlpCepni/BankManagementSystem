package bank.paymentservice.entity;

public enum BillType {
    ELECTRICITY("Elektrik"),
    WATER("Su"),
    NATURAL_GAS("Doğalgaz"),
    INTERNET("Internet"),
    PHONE("Telefon"),
    MOBILE("Mobil"),
    CREDIT_CARD("Kredi Kartı"),
    INSURANCE("Sigorta"),
    TAX("Vergi"),
    TRAFFIC_FINE("Trafik Cezası");

    private final String description;

    BillType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
