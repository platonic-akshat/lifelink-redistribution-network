package com.lifelink.outbox;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@AutoConfiguration
@EntityScan(basePackageClasses = OutboxEvent.class)
@EnableJpaRepositories(basePackageClasses = OutboxEventRepository.class)
@ComponentScan(basePackages = "com.lifelink.outbox")
@EnableScheduling
public class OutboxAutoConfiguration {
}
