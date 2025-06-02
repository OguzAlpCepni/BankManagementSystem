package bank.loanservice.service.impl;


import bank.loanservice.client.TransferClient;
import bank.loanservice.entity.LoanApplication;
import bank.loanservice.entity.LoanStatus;
import bank.loanservice.entity.Underwriting;
import bank.loanservice.kafka.producer.KafkaProducerService;
import bank.loanservice.repository.LoanApplicationRepository;
import bank.loanservice.repository.UnderWritingRepository;
import bank.loanservice.service.LoanService;
import io.github.oguzalpcepni.dto.LoansDto.LoanRequest;
import io.github.oguzalpcepni.dto.LoansDto.LoanStatusResponse;
import io.github.oguzalpcepni.dto.LoansDto.LoanTransferResponse;
import io.github.oguzalpcepni.dto.accountdto.LoanAccountDto;
import io.github.oguzalpcepni.dto.transferdto.TransferRequest;
import io.github.oguzalpcepni.dto.transferdto.TransferResponse;
import io.github.oguzalpcepni.event.LoanApplicationCreatedEvent;
import io.github.oguzalpcepni.event.LoanUnderwritingCompletedEvent;
import io.github.oguzalpcepni.event.LoanUnderwritingRejectedEvent;
import io.github.oguzalpcepni.exceptions.type.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final KafkaProducerService kafkaProducerService;
    private final UnderWritingRepository underWritingRepository;
    private final TransferClient transferClient;

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
        kafkaProducerService.sendCreatedEvent(loanApplicationCreatedEvent);

        LoanStatusResponse loanStatusResponse = new LoanStatusResponse();
        loanStatusResponse.setId(loanApplication.getId());
        loanStatusResponse.setExternalLoanId(loanApplication.getExternalLoanId());
        loanStatusResponse.setCreatedAt(loanApplication.getCreatedAt());
        loanStatusResponse.setStatus(String.valueOf(LoanStatus.PENDING));
        return loanStatusResponse;
    }
    public void onUnderwritingApproved(LoanUnderwritingCompletedEvent event) {

        LoanApplication loanApplication = loanApplicationRepository.findById(event.getLoanId()).orElseThrow(() -> new BusinessException("could not found any loanApplication"));

        Underwriting underwriting = new Underwriting();
        underwriting.setLoanApplication(loanApplication);
        underwriting.setCreditScore(event.getCreditScore());
        underwriting.setFraudCheckPassed(event.isFraudCheckPassed());
        underwriting.setEvaluatedAt(LocalDateTime.now());
        underwriting.setNotes("underwriting completed");
        underWritingRepository.save(underwriting);

        loanApplication.setStatus(LoanStatus.UNDERWRITTEN);
        loanApplicationRepository.save(loanApplication);
        // artık buradan sonra onay /transfer aşamasına geçiyoruz


    }
    @Transactional
    public LoanTransferResponse ApproveAndTransferMoney(UUID id, LoanAccountDto loanAccountDto){
        LoanApplication loanApplication = loanApplicationRepository.findById(id).orElseThrow(() -> new BusinessException("could not found any loanApplication"));

        // 1) Loan durumu hala UNDERWRITTEN mı kontrol et
        if (loanApplication.getStatus() != LoanStatus.UNDERWRITTEN) {
            throw new BusinessException("Loan is not in UNDERWRITTEN state: " + loanApplication.getId());
        }
        TransferRequest transferRequest = new TransferRequest();
        // bizim vermemiz gerekenler // buraya bir incele bakalım hata olabilir
        transferRequest.setSourceAccountId(UUID.fromString("19222397-bd78-4cf6-b633-0003538a3a58")); // banka id olarak dusun
        transferRequest.setTargetAccountId(loanAccountDto.getTargetAccountId());
        transferRequest.setSourceIban("TR79959859869241758261399250");
        transferRequest.setTargetIban(loanAccountDto.getTargetIban());
        transferRequest.setAmount(loanApplication.getAmount());
        transferRequest.setCurrency("TRY");
        transferRequest.setTransferType("INTERNAL");
        transferRequest.setDescription("loan transferred to you");
        transferRequest.setTransactionReference("TRX-45443612");

        LoanTransferResponse loanTransferResponse = new LoanTransferResponse();

        try {
            ResponseEntity<TransferResponse> transferResponse =transferClient.initiateTransfer(transferRequest);
            TransferResponse body = transferResponse.getBody();

            // Önce body null değil mi kontrol ediyoruz
            if (body != null && body.getStatus().equals("COMPLETED")) {
                loanApplication.setStatus(LoanStatus.APPROVED);
                loanTransferResponse.setNewStatus(String.valueOf(loanApplication.getStatus()));
                loanTransferResponse.setTransferStatus("COMPLETED");
                loanTransferResponse.setMessage("transfer successful");
            } else {
                // Ya body null, ya status COMPLETED değil: transfer başarısız kabul edebilirsin
                loanApplication.setStatus(LoanStatus.TRANSFER_FAILED);
                loanTransferResponse.setNewStatus(String.valueOf(loanApplication.getStatus()));
                loanTransferResponse.setTransferStatus("FAILED");
                loanTransferResponse.setMessage("transfer failed");

            }
        }catch (BusinessException businessException){
            // Feign/HTTP çağrısı sırasında exception fırladıysa
            loanApplication.setStatus(LoanStatus.TRANSFER_FAILED);
            loanTransferResponse.setNewStatus(String.valueOf(loanApplication.getStatus()));
            loanTransferResponse.setTransferStatus("FAILED");
            loanTransferResponse.setMessage("transfer failed");
            throw new BusinessException("Transfer sırasında hata: " + businessException.getMessage());
        }
        loanApplicationRepository.save(loanApplication);

        loanTransferResponse.setLoanId(loanApplication.getId());

        return loanTransferResponse;
    }
    @Override
    public void onUnderwritingRejected(LoanUnderwritingRejectedEvent event) {
        LoanApplication loanApplication = loanApplicationRepository.findById(event.getLoanId()).orElseThrow(() -> new BusinessException("could not found any loanApplication"));
        Underwriting underwriting = new Underwriting();
        underwriting.setLoanApplication(loanApplication);
        underwriting.setCreditScore(event.getCreditScore());
        underwriting.setFraudCheckPassed(event.isFraudCheckPassed());
        underwriting.setEvaluatedAt(LocalDateTime.now());
        underwriting.setNotes("underwriting rejected");
        underWritingRepository.save(underwriting);
        // 3) LoanApplication durumunu güncelle (REJECTED)
        loanApplication.setStatus(LoanStatus.REJECTED);
        loanApplicationRepository.save(loanApplication);



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
