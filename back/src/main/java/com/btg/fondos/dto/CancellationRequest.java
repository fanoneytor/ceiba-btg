package com.btg.fondos.dto;

import jakarta.validation.constraints.NotBlank;

public record CancellationRequest(
        @NotBlank String clientId,
        @NotBlank String fundId
) {
}
