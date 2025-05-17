package io.github.oguzalpcepni.dto.accountdto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreditRequest(BigDecimal amount, String currency, UUID transactionId, String description) {}
