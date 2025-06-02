package bank.loanservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "loan")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoanApplication {
    // ------ Primary Key ------
    @Id
    @UuidGenerator
    private UUID id;
    // ------ External Reference for UI / API ------
    @Column(name = "external_loan_id", nullable = false, unique = true, updatable = false)
    private UUID externalLoanId = UUID.randomUUID();//Genellikle dış dünyaya gösterilen "iş tanımlayıcı" gibi kullanılır.
    // ------ Business Fields ------
    @Column(nullable = false, updatable = false)
    private UUID customerId;
    @Column(nullable = false)
    private BigDecimal amount;
    @Column(nullable = false)
    private int installmentCount;
    @Column(length = 255)
    private String purpose;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;


    //audit
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
    // Relations
    @OneToMany(mappedBy = "loanApplication", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Installment> installments;

    @OneToOne(mappedBy = "loanApplication", cascade = CascadeType.ALL, orphanRemoval = true)
    private Underwriting underwriting;
}
