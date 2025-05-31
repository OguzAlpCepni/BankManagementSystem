package bank.transactionservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "loan_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanTransaction {

    @Id
    @UuidGenerator
    private UUID id;
    @Column(name = "loan_id", nullable = false, updatable = false)
    private UUID loanId;

    /**
     * Başvuru sahibinin UUID'si.
     */
    @Column(name = "customer_id", nullable = false, updatable = false)
    private UUID customerId;

    /**
     * Talep edilen kredi tutarı.
     */
    @Column(nullable = false)
    private java.math.BigDecimal amount;

    /**
     * Taksit sayısı.
     */
    @Column(name = "installment_count", nullable = false)
    private Integer installmentCount;

    /**
     * Müşterinin kredi skoru.
     */
    @Column(name = "credit_score")
    private Integer creditScore;

    /**
     * Fraud kontrolünden geçip geçmediğini gösterir.
     */
    @Column(name = "fraud_check_passed")
    private Boolean fraudCheckPassed;

    /**
     * Bu işlem için geçerli olan durum. (PENDING, APPROVED, REJECTED, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    /**
     * Oluşturulma zamanı.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Güncellenme zamanı.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
