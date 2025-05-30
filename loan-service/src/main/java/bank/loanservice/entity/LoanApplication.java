package bank.loanservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

    @Id
    @UuidGenerator
    private UUID id;

    private UUID loanId;

    private UUID customerId;
    private BigDecimal amount;
    private int installmentCount;
    private String purpose;
    @Enumerated(EnumType.STRING)
    private LoanStatus status;
    private BigDecimal interestRate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "loanApplication", cascade = CascadeType.ALL)
    private List<Installment> installments;

    @PrePersist
    protected void onCreate() {
        loanId = UUID.randomUUID();
        createdAt = LocalDateTime.now();
        status = LoanStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
