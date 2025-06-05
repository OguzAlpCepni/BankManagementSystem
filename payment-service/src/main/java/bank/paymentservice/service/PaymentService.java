package bank.paymentservice.service;

import io.github.oguzalpcepni.dto.payment.PaymentRequestDto;
import io.github.oguzalpcepni.dto.payment.PaymentResponseDto;
import io.github.oguzalpcepni.dto.payment.PaymentStatusUpdateDTO;

import java.util.UUID;

public interface PaymentService {
    /**
     * Yeni bir ödeme talebi oluşturur ve "PENDING" statüsüyle kaydeder.
     * Sonrasında Kafka'da PaymentRequestedEvent yayınlar.
     */
    PaymentResponseDto createPayment(PaymentRequestDto request);

    /**
     * Saga Orchestrator veya dış bir listener tarafından,
     * ödeme statüsü güncellemelerini dinleyip işleme sokar.
     */
    void updatePaymentStatus(PaymentStatusUpdateDTO statusUpdate);

    /**
     * Ödeme detaylarını ID ile (veya paymentReference ile) döner.
     */
    PaymentResponseDto getPaymentById(UUID paymentId);
}
