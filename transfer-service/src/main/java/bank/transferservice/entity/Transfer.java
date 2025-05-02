package bank.transferservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transfers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transfer extends BaseEntity {

    @Column(nullable = false)
    private UUID sourceAccountId;

    @Column(nullable = false)
    private String sourceIban;

    @Column(nullable = false)
    private UUID targetAccountId;

    @Column(nullable = false)
    private String targetIban;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Column(length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferStatus status;

    @Column
    private LocalDateTime completedAt;

    @Column
    private String errorMessage;

    @Column(nullable = false)
    private String transactionReference;

    // Saga pattern için işlem adımlarını takip etmek için
    @Column
    private Boolean sourceAccountDebited;

    @Column
    private Boolean targetAccountCredited;
} 