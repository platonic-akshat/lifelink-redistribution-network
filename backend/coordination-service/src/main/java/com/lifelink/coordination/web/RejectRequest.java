package com.lifelink.coordination.web;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RejectRequest(
        @NotNull UUID decidedBy,
        String reason
) {
}
