package com.lifelink.surplus.web;

import com.lifelink.security.LifelinkPrincipalAccessor;
import com.lifelink.security.OrganizationVerificationGate;
import com.lifelink.surplus.service.SurplusService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SurplusControllerAuthorizationTest {

    @Mock
    SurplusService surplusService;

    @Mock
    LifelinkPrincipalAccessor principalAccessor;

    @Mock
    OrganizationVerificationGate verificationGate;

    @InjectMocks
    SurplusController controller;

    @Test
    void createRejectsProviderIdThatDoesNotMatchOrganizationClaim() {
        UUID ownOrg = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID otherOrg = UUID.fromString("99999999-9999-9999-9999-999999999999");
        Authentication authentication = mock(Authentication.class);

        when(principalAccessor.requireOrganizationId(authentication)).thenReturn(ownOrg);

        CreateSurplusRequest request = new CreateSurplusRequest(
                com.lifelink.events.model.Category.FOOD,
                "Rice",
                10,
                "kg",
                19.0,
                72.0,
                Instant.parse("2030-01-01T00:00:00Z"),
                otherOrg,
                "Other Hospital");

        assertThrows(ResponseStatusException.class, () -> controller.create(request, authentication));
    }
}
