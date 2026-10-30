package com.lifelink.security;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@ConditionalOnProperty(name = "lifelink.verification.client-enabled", havingValue = "true")
@Import({LifelinkRestClientConfig.class, OrganizationVerificationGate.class})
public class LifelinkVerificationClientAutoConfiguration {
}
