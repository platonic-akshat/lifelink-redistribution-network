package com.lifelink.matching.topology;

import com.lifelink.events.model.*;
import com.lifelink.matching.KafkaTopics;
import com.lifelink.matching.serde.JacksonSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

/**
 * Core matching logic. Two KTables ("what surplus/need is currently active
 * per category+geohash bucket") are joined against each other's incoming
 * stream, so a match can be triggered by *either* side changing first —
 * a need arriving after surplus already exists, or surplus arriving after
 * a need is already open. See ARCHITECTURE.md §4.3 for the full design
 * rationale, including the documented geohash-cell-boundary limitation.
 */
@Configuration
public class MatchingTopology {

    private static final Serde<String> STRING_SERDE = Serdes.String();
    private static final JacksonSerde<SurplusEvent> SURPLUS_EVENT_SERDE = JacksonSerde.of(SurplusEvent.class);
    private static final JacksonSerde<NeedEvent> NEED_EVENT_SERDE = JacksonSerde.of(NeedEvent.class);
    private static final JacksonSerde<SurplusBucket> SURPLUS_BUCKET_SERDE = JacksonSerde.of(SurplusBucket.class);
    private static final JacksonSerde<NeedBucket> NEED_BUCKET_SERDE = JacksonSerde.of(NeedBucket.class);
    private static final JacksonSerde<MatchProposedEvent> MATCH_EVENT_SERDE = JacksonSerde.of(MatchProposedEvent.class);

    @Bean
    public KStream<String, MatchProposedEvent> matchProposedStream(StreamsBuilder streamsBuilder) {
        KStream<String, SurplusEvent> surplusByBucket = streamsBuilder
                .stream(KafkaTopics.SURPLUS_EVENTS, Consumed.with(STRING_SERDE, SURPLUS_EVENT_SERDE))
                .selectKey((surplusId, event) -> bucketKey(event.category(), event.geohash5()));

        KStream<String, NeedEvent> needByBucket = streamsBuilder
                .stream(KafkaTopics.NEED_EVENTS, Consumed.with(STRING_SERDE, NEED_EVENT_SERDE))
                .selectKey((needId, event) -> bucketKey(event.category(), event.geohash5()));

        KTable<String, SurplusBucket> surplusTable = surplusByBucket
                .groupByKey(Grouped.with(STRING_SERDE, SURPLUS_EVENT_SERDE))
                .aggregate(SurplusBucket::empty, (bucketKey, event, bucket) -> bucket.apply(event),
                        Materialized.<String, SurplusBucket, KeyValueStore<Bytes, byte[]>>as("surplus-bucket-store")
                                .withKeySerde(STRING_SERDE)
                                .withValueSerde(SURPLUS_BUCKET_SERDE));

        KTable<String, NeedBucket> needTable = needByBucket
                .groupByKey(Grouped.with(STRING_SERDE, NEED_EVENT_SERDE))
                .aggregate(NeedBucket::empty, (bucketKey, event, bucket) -> bucket.apply(event),
                        Materialized.<String, NeedBucket, KeyValueStore<Bytes, byte[]>>as("need-bucket-store")
                                .withKeySerde(STRING_SERDE)
                                .withValueSerde(NEED_BUCKET_SERDE));

        // Direction 1: a need arrives (or updates) -> score it against every
        // currently-available surplus item in its bucket.
        KStream<String, MatchProposedEvent> matchesOnNeedArrival = needByBucket
                .filter((bucketKey, need) -> need.status() == NeedStatus.POSTED)
                .join(surplusTable,
                        (need, bucket) -> MatchScorer.bestMatchForNeed(need, bucket.items()),
                        Joined.with(STRING_SERDE, NEED_EVENT_SERDE, SURPLUS_BUCKET_SERDE))
                .filter((bucketKey, match) -> match.isPresent())
                .mapValues(Optional::get);

        // Direction 2: surplus arrives (or updates) -> score it against every
        // currently-open need in its bucket. Without this direction, surplus
        // posted *after* a matching need would never trigger a proposal.
        KStream<String, MatchProposedEvent> matchesOnSurplusArrival = surplusByBucket
                .filter((bucketKey, surplus) -> surplus.status() == SurplusStatus.POSTED
                        || surplus.status() == SurplusStatus.EXPIRING_SOON)
                .join(needTable,
                        (surplus, bucket) -> MatchScorer.bestMatchForSurplus(surplus, bucket.items()),
                        Joined.with(STRING_SERDE, SURPLUS_EVENT_SERDE, NEED_BUCKET_SERDE))
                .filter((bucketKey, match) -> match.isPresent())
                .mapValues(Optional::get);

        KStream<String, MatchProposedEvent> merged = matchesOnNeedArrival.merge(matchesOnSurplusArrival)
                .selectKey((bucketKey, match) -> match.matchId().toString());

        merged.to(KafkaTopics.MATCH_PROPOSED, Produced.with(STRING_SERDE, MATCH_EVENT_SERDE));
        return merged;
    }

    private static String bucketKey(Category category, String geohash5) {
        return category.name() + "|" + geohash5;
    }
}
