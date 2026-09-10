package com.lifelink.matching.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelink.events.kafka.EventObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

/** Generic Kafka Streams Serde backed by the shared {@link EventObjectMapper}. */
public final class JacksonSerde<T> implements Serde<T> {

    private final ObjectMapper mapper = EventObjectMapper.create();
    private final Class<T> type;

    public JacksonSerde(Class<T> type) {
        this.type = type;
    }

    public static <T> JacksonSerde<T> of(Class<T> type) {
        return new JacksonSerde<>(type);
    }

    @Override
    public Serializer<T> serializer() {
        return (topic, data) -> {
            if (data == null) return null;
            try {
                return mapper.writeValueAsBytes(data);
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize " + type.getSimpleName(), e);
            }
        };
    }

    @Override
    public Deserializer<T> deserializer() {
        return (topic, bytes) -> {
            if (bytes == null) return null;
            try {
                return mapper.readValue(bytes, type);
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize " + type.getSimpleName(), e);
            }
        };
    }
}
