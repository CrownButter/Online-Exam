package com.onlineexam.identity.api.dto;

import com.onlineexam.identity.domain.entity.Organization;

public record OrganizationResponse(
        Long id,
        String name,
        String slug,
        String status
) {
    public static OrganizationResponse from(Organization organization) {
        return new OrganizationResponse(
                organization.getId(),
                organization.getName(),
                organization.getSlug(),
                organization.getStatus().name()
        );
    }
}
