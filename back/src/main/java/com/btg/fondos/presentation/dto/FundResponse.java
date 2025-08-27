package com.btg.fondos.presentation.dto;

import com.btg.fondos.domain.enums.FundCategory;

import java.math.BigDecimal;

public record FundResponse(
        String id,
        String name,
        BigDecimal minimumAmount,
        FundCategory category
) {
}
