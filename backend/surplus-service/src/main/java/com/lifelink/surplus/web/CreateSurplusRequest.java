package com.lifelink.surplus.web;

import com.lifelink.events.model.Category;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.UUID;

public record CreateSurplusRequest(
        @NotNull Category category,
        @NotBlank String subType,
        @Positive double quantity,
        @NotBlank String unit,
        @DecimalMin("-90") @DecimalMax("90") double latitude,
        @DecimalMin("-180") @DecimalMax("180") double longitude,
        @NotNull @Future Instant expiresAt,
        @NotNull UUID providerId,
        @NotBlank String providerName
) {
}
