package bank.transferservice.entity;

/**
 * Transfer işleminin tipini belirtir.
 */
public enum TransferType {
    INTERNAL,  // Aynı banka içi havale
    EFT,       // Elektronik Fon Transferi (farklı bankalar arası)
    SWIFT,     // Uluslararası para transferi
    FAST       // Anlık para transferi
} 