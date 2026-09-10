package com.lifelink.events.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Every service builds its Kafka {@code JsonSerializer}/{@code JsonDeserializer}
 * from this ObjectMapper so event wire format (timestamps in particular) is
 * identical producer-to-consumer. Centralizing it here is what keeps
 * {@code common-events} a real shared contract instead of duplicated DTOs.
 */
public final class EventObjectMapper {

    private EventObjectMapper() {
    }

    public static ObjectMapper create() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
