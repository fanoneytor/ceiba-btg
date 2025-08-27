package com.btg.fondos.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record SubscriptionRequest(
        @NotBlank String clientId,
        @NotBlank String fundId,
        @Positive BigDecimal amount
) {
}
