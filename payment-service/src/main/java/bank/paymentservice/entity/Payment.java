package bank.paymentservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "payment_reference", unique = true, nullable = false)
    private String paymentReference;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    // Fatura tipi (elektrik, su, doğal gaz, vb.)
    @Enumerated(EnumType.STRING)
    @Column(name = "bill_type", nullable = false)
    private BillType billType;

    @Column(name = "biller_code", nullable = false)
    private String billerCode;

    @Column(name = "subscriber_number", nullable = false)
    private String subscriberNumber;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "bill_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal billAmount;

    @Column(name = "commission_amount", precision = 15, scale = 2)
    private BigDecimal commissionAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "bill_description")
    private String billDescription;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "transaction_id")       // Harici ödeme sağlayıcı geribildirimi için
    private String transactionId;

    @Column(name = "biller_response")
    private String billerResponse;

    @Column(name = "error_message")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Version
    private Long version;
}
