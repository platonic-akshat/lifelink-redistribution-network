package com.lifelink.events;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MatchIdsTest {

    @Test
    void deterministicMatchIdIsStableForSamePair() {
        UUID needId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID surplusId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID first = MatchIds.deterministicMatchId(needId, List.of(surplusId));
        UUID second = MatchIds.deterministicMatchId(needId, List.of(surplusId));
        assertEquals(first, second);
    }
}
