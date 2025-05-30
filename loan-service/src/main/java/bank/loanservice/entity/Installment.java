package bank.loanservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "installments")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Installment {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "loan_application_id")
    private LoanApplication loanApplication;

    private int installmentNumber;

    private LocalDateTime dueDate;

    private BigDecimal principalAmount;

    private BigDecimal interestAmount;

    private BigDecimal totalAmount;

    private boolean paid;
}
