package com.lifelink.events;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Stable identifiers for match proposals so rescoring the same need/surplus pair
 * does not create duplicate coordinator rows.
 */
public final class MatchIds {

    private MatchIds() {
    }

    public static UUID deterministicMatchId(UUID needId, List<UUID> surplusIds) {
        if (surplusIds == null || surplusIds.isEmpty()) {
            throw new IllegalArgumentException("surplusIds must not be empty");
        }
        UUID primarySurplus = surplusIds.getFirst();
        String key = needId + "|" + primarySurplus;
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
    }
}
