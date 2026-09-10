package com.lifelink.surplus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SurplusServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SurplusServiceApplication.class, args);
    }
}
