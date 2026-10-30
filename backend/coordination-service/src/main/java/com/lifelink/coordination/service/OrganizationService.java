package com.lifelink.coordination.service;

import com.lifelink.coordination.domain.Organization;
import com.lifelink.coordination.domain.OrganizationVerificationStatus;
import com.lifelink.coordination.repository.OrganizationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class OrganizationService {

    private final OrganizationRepository repository;

    public OrganizationService(OrganizationRepository repository) {
        this.repository = repository;
    }

    public Organization getOrThrow(UUID organizationId) {
        return repository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found: " + organizationId));
    }

    @Transactional
    public Organization updateVerification(UUID organizationId, OrganizationVerificationStatus status) {
        Organization organization = getOrThrow(organizationId);
        organization.updateVerification(status, Instant.now());
        return organization;
    }
}
