package com.onlineexam.identity.application.service;

import com.onlineexam.identity.domain.entity.Organization;
import com.onlineexam.identity.domain.repository.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public Organization create(String name, String slug) {
        if (organizationRepository.findBySlug(slug).isPresent()) {
            throw new IllegalArgumentException("Organization slug already exists: " + slug);
        }
        return organizationRepository.save(new Organization(name, slug));
    }

    @Transactional(readOnly = true)
    public Organization get(Long organizationId) {
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found: " + organizationId));
    }

    public Organization rename(Long organizationId, String name) {
        Organization organization = get(organizationId);
        organization.rename(name);
        return organization;
    }

    public Organization changeSlug(Long organizationId, String slug) {
        Organization organization = get(organizationId);
        if (!organization.getSlug().equals(slug) && organizationRepository.findBySlug(slug).isPresent()) {
            throw new IllegalArgumentException("Organization slug already exists: " + slug);
        }
        organization.changeSlug(slug);
        return organization;
    }

    public Organization suspend(Long organizationId) {
        Organization organization = get(organizationId);
        organization.suspend();
        return organization;
    }

    public Organization activate(Long organizationId) {
        Organization organization = get(organizationId);
        organization.activate();
        return organization;
    }
}
