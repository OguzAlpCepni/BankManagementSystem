# Transfer Service

Bu servis, para transferi işlemlerini yönetmek için oluşturulmuştur.

## Feign Client Entegrasyonu

Transfer Service, Account Service ile iletişim kurmak için Feign Client kullanmaktadır. Bu entegrasyon aşağıdaki işlevleri sağlar:

- Hesap varlığını doğrulama
- Hesap bakiyelerini kontrol etme
- Hesap hareketlerini izleme

### AccountServiceClient Arayüzü

```java
@FeignClient(name = "ACCOUNT-SERVICE")
public interface AccountServiceClient {
    @GetMapping("/api/v1/accounts/{id}")
    ResponseEntity<?> getAccountById(@PathVariable UUID id);

    @GetMapping("/api/v1/accounts/iban/{iban}")
    ResponseEntity<?> getAccountByIban(@PathVariable String iban);

    @GetMapping("/api/v1/accounts/{id}/validate")
    ResponseEntity<?> validateAccount(@PathVariable UUID id);

    @GetMapping("/api/v1/accounts/iban/{iban}/validate")
    ResponseEntity<?> validateAccountByIban(@PathVariable String iban);

    @GetMapping("/api/v1/accounts/{id}/balance-check")
    ResponseEntity<Boolean> checkBalance(
            @PathVariable UUID id,
            @RequestParam BigDecimal amount);
}
```

## Kafka Entegrasyonu

Transfer Service, olay tabanlı iletişim için Kafka kullanmaktadır. Bu, mikroservis mimarisinde aşağıdaki avantajları sağlar:

- Servisler arası asenkron iletişim
- Yüksek ölçeklenebilirlik
- Bileşenler arasında gevşek bağlama (loose coupling)

### Olaylar

- `TransferEvent`: Transfer işlemlerinin farklı aşamalarını bildirmek için kullanılır.

### Kafka Producer ve Consumer

- `KafkaProducerService`: Transfer olaylarını Kafka'ya göndermek için kullanılır.
- `KafkaConsumer`: Transfer olaylarını dinlemek ve işlemek için kullanılır.

## Saga Pattern Entegrasyonu

Transfer servisi, dağıtık işlemlerin tutarlılığını sağlamak için Saga Pattern'i kullanmaktadır. İşlem adımları şunlardır:

1. Transfer isteği alınır ve doğrulanır
2. Kaynak hesaptan para çekilir
3. Hedef hesaba para yatırılır
4. Transfer durumu güncellenir

Herhangi bir adım başarısız olursa, önceki adımlar telafi edilir (compensating transactions).

## Servis Mimarisi

```
TransferController -> TransferService -> KafkaProducerService -> Kafka -> KafkaConsumer -> TransferProcessorService
                  |                   |
                  v                   v
          TransferRepository    AccountServiceClient
```

## Servis Durumları

Transfer işlemleri aşağıdaki durumlarda olabilir:

- `PENDING`: Transfer başlatıldı, henüz işlenmedi
- `COMPLETED`: Transfer başarıyla tamamlandı
- `FAILED`: Transfer başarısız oldu
- `CANCELLED`: Transfer iptal edildi
- `REFUNDED`: Transfer iade edildi 