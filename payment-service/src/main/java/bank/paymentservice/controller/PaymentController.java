package bank.paymentservice.controller;

import bank.paymentservice.service.PaymentService;
import io.github.oguzalpcepni.dto.payment.PaymentRequestDto;
import io.github.oguzalpcepni.dto.payment.PaymentResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/pay")
    public ResponseEntity<Map<String,Object>> pay(
            @RequestBody Map<String,Object> req
    ) {
        boolean success = Math.random() < 0.9;
        if (success) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "transactionId", UUID.randomUUID().toString(),
                    "message", "Payment processed (mock)"
            ));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "Mock billing failure"
            ));
        }
    }

    @PostMapping("/payBill")
    public ResponseEntity<PaymentResponseDto> createPayment(PaymentRequestDto paymentRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPayment(paymentRequestDto));
    }

}
