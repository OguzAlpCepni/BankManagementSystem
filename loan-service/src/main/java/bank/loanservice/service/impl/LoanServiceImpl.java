package bank.loanservice.service.impl;

import bank.loanservice.entity.LoanApplication;
import bank.loanservice.entity.LoanStatus;
import bank.loanservice.kafka.producer.KafkaProducerService;
import bank.loanservice.repository.LoanApplicationRepository;
import bank.loanservice.service.LoanService;
import io.github.oguzalpcepni.dto.LoansDto.LoanRequest;
import io.github.oguzalpcepni.dto.LoansDto.LoanStatusResponse;
import io.github.oguzalpcepni.event.LoanApplicationCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final KafkaProducerService kafkaProducerService;

    @Override
    public LoanStatusResponse createLoan(LoanRequest loanRequest) {
        UUID customerId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setCustomerId(customerId);
        loanApplication.setAmount(loanRequest.getAmount());
        loanApplication.setInstallmentCount(loanRequest.getInstallmentCount());
        loanApplication.setPurpose(loanApplication.getPurpose());
        loanApplication.setStatus(LoanStatus.PENDING);

        loanApplicationRepository.save(loanApplication);

        // event yayınlamak için gerekli map işlemi
        LoanApplicationCreatedEvent loanApplicationCreatedEvent = loanApplicationMapToEvent(loanApplication);
        // kafka ile bu eventi yayınla
        kafkaProducerService.sendTransferEvent(loanApplicationCreatedEvent);

        LoanStatusResponse loanStatusResponse = new LoanStatusResponse();
        loanStatusResponse.setId(loanApplication.getId());
        loanStatusResponse.setExternalLoanId(loanApplication.getExternalLoanId());
        loanStatusResponse.setCreatedAt(loanApplication.getCreatedAt());
        loanStatusResponse.setStatus(String.valueOf(LoanStatus.PENDING));
        return loanStatusResponse;
    }
    /**
     * Çok basit bir örnek: customerId'nin hash koduna göre 300-850 aralığında bir puan döner.
     * Böylece aynı customerId için hep aynı skor elde edilmiş olur.
     *
     * NOT: Gerçek dünyada böyle kullanılmamalıdır; bu sadece POC/demo amaçlı bir stub'tur.
     *
     * @param customerId Kredi skorunu hesaplayacağımız müşteri UUID'si
     * @return 300 ile 850 (dahil) arasında bir int kredi skoru
     */
    public int calculateCreditScore(UUID customerId) {
        // UUID'nin hashCode() değeri pozitif veya negatif olabilir.
        int raw = customerId.hashCode();

        // Negatif olma ihtimaline karşı mutlak değerini alıyoruz.
        int absHash = Math.abs(raw);

        // 300 ile 850 arasında sonuç üretmek için:
        // Puan aralığımızın genişliği = 850 - 300 + 1 = 551
        // absHash % 551 ==> 0..550 arası bir değer
        int scoreInRange = (absHash % 551) + 300;

        return scoreInRange;
    }


    private LoanApplicationCreatedEvent loanApplicationMapToEvent(LoanApplication loanApplication) {
        LoanApplicationCreatedEvent loanApplicationCreatedEvent = new LoanApplicationCreatedEvent();
        loanApplicationCreatedEvent.setCustomerId(loanApplication.getCustomerId());
        loanApplicationCreatedEvent.setAmount(loanApplication.getAmount());
        loanApplicationCreatedEvent.setInstallmentCount(loanApplication.getInstallmentCount());
        loanApplicationCreatedEvent.setPurpose(loanApplication.getPurpose());
        return loanApplicationCreatedEvent;
    }

}
