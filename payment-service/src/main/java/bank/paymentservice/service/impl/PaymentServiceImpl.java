package bank.paymentservice.service.impl;

import bank.paymentservice.entity.BillType;
import bank.paymentservice.entity.Payment;
import bank.paymentservice.entity.PaymentMethod;
import bank.paymentservice.entity.PaymentStatus;
import bank.paymentservice.kafka.KafkaProducerService;
import bank.paymentservice.repository.PaymentRepository;
import bank.paymentservice.service.PaymentService;
import io.github.oguzalpcepni.dto.payment.PaymentRequestDto;
import io.github.oguzalpcepni.dto.payment.PaymentResponseDto;
import io.github.oguzalpcepni.dto.payment.PaymentStatusUpdateDTO;
import io.github.oguzalpcepni.exceptions.type.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaProducerService kafkaProducerService;

    @Override
    @Transactional
    public PaymentResponseDto createPayment(PaymentRequestDto request) {
        Payment payment = mapToPayment(request);
        Payment saved = paymentRepository.save(payment);
        kafkaProducerService.publishPaymentRequested(payment);
        log.info("Payment created with ID: {} and PaymentReference: {}", saved.getId(), saved.getPaymentReference());
        return mapToDto(payment);
    }

    @Override
    public void updatePaymentStatus(PaymentStatusUpdateDTO statusUpdate) {
        UUID id = statusUpdate.getPaymentId();
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Payment not found: " + id));

        payment.setPaymentStatus(PaymentStatus.valueOf(statusUpdate.getNewStatus()));
        payment.setBillerResponse(statusUpdate.getBillerResponse());
        payment.setErrorMessage(statusUpdate.getErrorMessage());
        payment.setProcessedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        log.info("Payment ID {} status updated to {}", id, statusUpdate.getNewStatus());
    }

    @Transactional()
    public PaymentResponseDto getPaymentById(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException("Payment not found: " + paymentId));
        return mapToDto(payment);
    }

    private Payment mapToPayment(PaymentRequestDto paymentRequestDto) {
        Payment payment = new Payment();

        payment.setAmount(paymentRequestDto.getAmount());
        payment.setPaymentReference(UUID.randomUUID().toString()); // Unique referans
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUserId(paymentRequestDto.getUserId());
        payment.setAccountId(paymentRequestDto.getAccountId());

        payment.setBillType(BillType.valueOf(paymentRequestDto.getBillType()));
        payment.setBillerCode(paymentRequestDto.getBillerCode());
        payment.setSubscriberNumber(paymentRequestDto.getSubscriberNumber());
        payment.setBillDescription(paymentRequestDto.getBillDescription());
        payment.setDueDate(paymentRequestDto.getDueDate());
        payment.setPaymentMethod(PaymentMethod.valueOf(paymentRequestDto.getPaymentMethod()));
        payment.setTransactionId(payment.getTransactionId());
        payment.setBillerResponse(payment.getBillerResponse());
        payment.setErrorMessage(payment.getErrorMessage());
        payment.setCreatedAt(payment.getCreatedAt());
        return payment;
    }

    private PaymentResponseDto mapToDto(Payment payment) {
        return PaymentResponseDto.builder()
                .id(payment.getId())
                .paymentReference(payment.getPaymentReference())
                .paymentStatus(payment.getPaymentStatus().name())
                .amount(payment.getAmount())
                .billAmount(payment.getAmount()) // FIXME: Gerçek projede fatura tutarı ayrı olmalı
                .commissionAmount(payment.getCommissionAmount())
                .createdAt(payment.getCreatedAt())
                .processedAt(payment.getProcessedAt())
                .errorMessage(payment.getErrorMessage())
                .build();

    }
}

