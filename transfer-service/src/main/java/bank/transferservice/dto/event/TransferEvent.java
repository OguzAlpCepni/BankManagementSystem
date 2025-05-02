package bank.transferservice.dto.event;

import bank.transferservice.entity.TransferStatus;
import bank.transferservice.entity.TransferType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferEvent {
    private UUID transferId;
    private UUID sourceAccountId;
    private UUID targetAccountId;
    private String sourceIban;
    private String targetIban;
    private BigDecimal amount;
    private String currency;
    private TransferType type;
    private TransferStatus status;
    private String description;
    private String transactionReference;
    private Boolean sourceAccountDebited;
    private Boolean targetAccountCredited;
} 