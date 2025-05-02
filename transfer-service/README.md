# Transfer Service

Transfer Service, Banka Yönetim Sistemi mikroservis mimarisi içinde para transferlerini yönetmekten sorumlu servistir.

## Özellikler

- Havale (Aynı banka içi transfer)
- EFT (Elektronik Fon Transferi)
- SWIFT (Uluslararası transferler)
- FAST (Anlık para transferi)
- Saga pattern ile dağıtık işlem yönetimi
- Feign Client ile AccountService entegrasyonu
- Kafka ile event tabanlı iletişim

## Teknik Detaylar

### Mikroservis Mimarisi

Bu servis, Banka Yönetim Sistemi mikroservis mimarisinin bir parçasıdır. Diğer servislerle iletişim için Feign Client kullanır ve event-driven iletişim için Kafka kullanır.

### Feign Client Entegrasyonu

Transfer servisi, hesap işlemleri için Account Service ile iletişim kurar. Bu entegrasyon, aşağıdaki işlevleri sağlar:

- Hesapları doğrulama
- Bakiye kontrolü
- Para çekme/yatırma işlemleri

### Kafka Entegrasyonu

Transfer servisi, aşağıdaki durumlar için Kafka event'leri üretir ve tüketir:

- Transfer başlatma
- Transfer tamamlanma
- Transfer başarısız olma
- Transfer iptal edilme
- Transfer iade edilme

### Saga Pattern

Transfer işlemleri, dağıtık sistemlerde tutarlılık sağlamak için Saga pattern kullanır. Bir transfer işlemi şu adımlardan oluşur:

1. Transfer isteği alınır
2. Kaynak hesaptan para çekilir
3. Hedef hesaba para yatırılır
4. Transfer tamamlanır

Herhangi bir adımda hata olursa, önceki adımlar telafi işlemleri (compensating transactions) ile geri alınır. Örneğin, kaynak hesaptan para çekildikten sonra hedef hesaba yatırılamazsa, çekilen para kaynak hesaba iade edilir.

## Servis Endpoints

### Transfer İşlemleri

- `POST /api/v1/transfers` - Yeni transfer başlatma
- `GET /api/v1/transfers/{id}` - Transfer detaylarını getirme
- `GET /api/v1/transfers/source/{iban}` - Kaynak IBAN'a göre transferleri getirme
- `GET /api/v1/transfers/target/{iban}` - Hedef IBAN'a göre transferleri getirme
- `GET /api/v1/transfers/status/{status}` - Duruma göre transferleri getirme
- `POST /api/v1/transfers/{id}/cancel` - Transfer iptal etme

## Örnek Transfer Akışı

1. Kullanıcı, TransferRequest ile bir transfer isteği yapar
2. Transfer servisi, kaynak hesabın bakiyesini kontrol eder
3. Yeterli bakiye varsa, bir Transfer kaydı oluşturulur ve PENDING durumuna ayarlanır
4. Kaynak hesaptan para çekilir ve SourceAccountDebited=true olarak işaretlenir
5. Hedef hesaba para yatırılır ve TargetAccountCredited=true olarak işaretlenir
6. Transfer, COMPLETED durumuna güncellenir ve işlem tamamlanır

Herhangi bir adımda hata olursa, ilgili telafi işlemleri yapılır ve Transfer FAILED veya REFUNDED durumuna güncellenir. 