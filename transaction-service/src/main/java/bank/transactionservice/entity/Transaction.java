package bank.transactionservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    // Hesap bilgileri
    @Column(nullable = false)
    private UUID sourceAccountId;
    
    @Column(nullable = false)
    private UUID targetAccountId;
    
    @Column(nullable = false)
    private String sourceIban;
    
    @Column(nullable = false)
    private String targetIban;
    // Ödeme detayları
    @Column(nullable = false)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private String currency;
    
    @Column(nullable = false)
    private String description;
    // Durum yönetimi
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime completedAt;
    
    private LocalDateTime failedAt;
    
    @Column(nullable = false)
    private boolean sourceAccountDebited;
    
    @Column(nullable = false)
    private boolean targetAccountCredited;
    
    private String errorMessage;
    
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = TransactionStatus.PENDING;
        }
    }
} 