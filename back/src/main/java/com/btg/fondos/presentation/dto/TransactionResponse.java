package com.btg.fondos.presentation.dto;

import com.btg.fondos.domain.enums.TransactionStatus;
import com.btg.fondos.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        String id,
        String transactionId,
        String clientId,
        String fundId,
        TransactionType type,
        BigDecimal amount,
        TransactionStatus status,
        String message,
        Instant date
) {
}
