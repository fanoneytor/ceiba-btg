package com.btg.fondos.dto;

import com.btg.fondos.domain.enums.NotificationChannel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ClientResponse(
        String id,
        String name,
        String email,
        String phone,
        BigDecimal availableBalance,
        NotificationChannel preferredNotification,
        List<ActiveSubscriptionResponse> activeFunds,
        Instant createdAt,
        Instant updatedAt
) {
}
