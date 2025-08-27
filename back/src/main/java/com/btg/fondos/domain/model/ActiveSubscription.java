package com.btg.fondos.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiveSubscription {
    @NotBlank
    private String fundId;
    @Positive
    private BigDecimal investedAmount;
    private Instant subscriptionDate;

}
