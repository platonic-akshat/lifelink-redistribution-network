package com.lifelink.need.web;

import com.lifelink.events.model.Category;
import com.lifelink.events.model.Urgency;
import jakarta.validation.constraints.*;

import java.util.UUID;

public record CreateNeedRequest(
        @NotNull Category category,
        @NotBlank String subType,
        @Positive double quantity,
        @NotBlank String unit,
        @NotNull Urgency urgency,
        @DecimalMin("-90") @DecimalMax("90") double latitude,
        @DecimalMin("-180") @DecimalMax("180") double longitude,
        @NotNull UUID requesterId,
        @NotBlank String requesterName
) {
}
