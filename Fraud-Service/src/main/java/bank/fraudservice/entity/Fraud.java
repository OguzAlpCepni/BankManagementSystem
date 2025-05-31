package bank.fraudservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;


@Entity
@Table(name = "fraud")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Fraud {

    @Id
    @UuidGenerator
    private UUID id;

    private UUID loanId;

    private BigDecimal amount;

    private int creditScore;
    private boolean isFraud;

}
