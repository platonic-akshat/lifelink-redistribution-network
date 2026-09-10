package com.lifelink.matching.topology;

import com.lifelink.events.geo.GeoUtil;
import com.lifelink.events.model.MatchProposedEvent;
import com.lifelink.events.model.NeedEvent;
import com.lifelink.events.model.SurplusEvent;
import com.lifelink.events.model.Urgency;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Scores candidate surplus-need pairs by urgency + proximity + time-to-expiry.
 * Weights are tuned for "urgency dominates, but a far-away or almost-expired
 * item should still lose to a closer/fresher one" — see ARCHITECTURE.md §4.3.
 * A real deployment would tune these against historical fulfillment data
 * rather than hand-picked constants.
 */
public final class MatchScorer {

    private static final double URGENCY_WEIGHT = 10.0;
    private static final double PROXIMITY_WEIGHT = 50.0;
    private static final double EXPIRY_WEIGHT = 20.0;

    private MatchScorer() {
    }

    public static Optional<MatchProposedEvent> bestMatchForNeed(NeedEvent need, Map<UUID, SurplusEvent> candidates) {
        return candidates.values().stream()
                .filter(s -> s.quantity() >= need.quantity())
                .map(s -> toMatch(need, s))
                .max(Comparator.comparingDouble(MatchProposedEvent::score));
    }

    public static Optional<MatchProposedEvent> bestMatchForSurplus(SurplusEvent surplus, Map<UUID, NeedEvent> candidates) {
        return candidates.values().stream()
                .filter(n -> surplus.quantity() >= n.quantity())
                .map(n -> toMatch(n, surplus))
                .max(Comparator.comparingDouble(MatchProposedEvent::score));
    }

    private static MatchProposedEvent toMatch(NeedEvent need, SurplusEvent surplus) {
        double distanceKm = GeoUtil.distanceKm(need.location(), surplus.location());
        double hoursToExpiry = Math.max(0, Duration.between(Instant.now(), surplus.expiresAt()).toMinutes() / 60.0);
        double score = URGENCY_WEIGHT * urgencyWeight(need.urgency())
                + PROXIMITY_WEIGHT * (1.0 / (distanceKm + 0.1))
                + EXPIRY_WEIGHT * (1.0 / (hoursToExpiry + 1.0));

        return new MatchProposedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                need.needId(),
                List.of(surplus.surplusId()),
                score,
                distanceKm,
                hoursToExpiry,
                Instant.now()
        );
    }

    private static double urgencyWeight(Urgency urgency) {
        return switch (urgency) {
            case CRITICAL -> 3.0;
            case HIGH -> 2.0;
            case NORMAL -> 1.0;
        };
    }
}
