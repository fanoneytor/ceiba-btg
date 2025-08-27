package com.btg.fondos.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class ActiveSubscription {
    @NotBlank
    private String fundId;
    @Positive
    private BigDecimal investedAmount;
    private Instant subscriptionDate;

}
