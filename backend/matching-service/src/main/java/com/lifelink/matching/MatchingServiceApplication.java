package com.lifelink.matching;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@SpringBootApplication
@EnableKafkaStreams
public class MatchingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MatchingServiceApplication.class, args);
    }
}
