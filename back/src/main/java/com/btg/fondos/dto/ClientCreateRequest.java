package com.btg.fondos.dto;

import com.btg.fondos.domain.enums.NotificationChannel;

import java.math.BigDecimal;

public record ClientCreateRequest(
        String name,
        String email,
        String phone,
        BigDecimal initialBalance,
        NotificationChannel preferredNotification
) {
}