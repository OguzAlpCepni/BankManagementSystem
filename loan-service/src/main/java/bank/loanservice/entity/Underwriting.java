package bank.loanservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "underwritings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Underwriting {

    @Id
    @UuidGenerator
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false, unique = true)
    private LoanApplication loanApplication;

    @Column(nullable = false)
    private Integer creditScore;

    @Column(nullable = false)
    private Boolean fraudCheckPassed;

    @Column(nullable = false)
    private LocalDateTime evaluatedAt = LocalDateTime.now();

    @Column(length = 500)
    private String notes;
}
