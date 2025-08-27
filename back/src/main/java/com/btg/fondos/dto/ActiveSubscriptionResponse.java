package com.btg.fondos.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ActiveSubscriptionResponse(
        String fundId,
        String fundName,
        BigDecimal investedAmount,
        Instant subscriptionDate
) {
}
