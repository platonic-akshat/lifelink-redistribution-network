package com.lifelink.matching.topology;

import com.lifelink.events.model.SurplusEvent;
import com.lifelink.events.model.SurplusStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Aggregate value for the {@code surplus-bucket-store} KTable: every
 * currently-active (POSTED / EXPIRING_SOON) surplus item seen so far for one
 * {@code category|geohash5} bucket, keyed by surplusId. Items are removed
 * once claimed/expired/completed so the table only ever reflects "still
 * available right now".
 */
public record SurplusBucket(Map<UUID, SurplusEvent> items) {

    public static SurplusBucket empty() {
        return new SurplusBucket(Map.of());
    }

    public SurplusBucket apply(SurplusEvent event) {
        Map<UUID, SurplusEvent> next = new HashMap<>(items);
        if (event.status() == SurplusStatus.POSTED || event.status() == SurplusStatus.EXPIRING_SOON) {
            next.put(event.surplusId(), event);
        } else {
            next.remove(event.surplusId());
        }
        return new SurplusBucket(next);
    }
}
