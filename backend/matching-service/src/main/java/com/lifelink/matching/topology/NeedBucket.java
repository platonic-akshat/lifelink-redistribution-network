package com.lifelink.matching.topology;

import com.lifelink.events.model.NeedEvent;
import com.lifelink.events.model.NeedStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Aggregate value for the {@code need-bucket-store} KTable: every currently
 * open (POSTED) need seen so far for one {@code category|geohash5} bucket,
 * keyed by needId.
 */
public record NeedBucket(Map<UUID, NeedEvent> items) {

    public static NeedBucket empty() {
        return new NeedBucket(Map.of());
    }

    public NeedBucket apply(NeedEvent event) {
        Map<UUID, NeedEvent> next = new HashMap<>(items);
        if (event.status() == NeedStatus.POSTED) {
            next.put(event.needId(), event);
        } else {
            next.remove(event.needId());
        }
        return new NeedBucket(next);
    }
}
