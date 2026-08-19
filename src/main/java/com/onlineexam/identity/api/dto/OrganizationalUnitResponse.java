package com.onlineexam.identity.api.dto;

import com.onlineexam.identity.domain.entity.OrganizationalUnit;

public record OrganizationalUnitResponse(
        Long id,
        Long organizationId,
        Long parentUnitId,
        String name,
        String type) {

    public static OrganizationalUnitResponse from(OrganizationalUnit unit) {
        return new OrganizationalUnitResponse(
                unit.getId(),
                unit.getOrganization().getId(),
                unit.getParentUnit() == null ? null : unit.getParentUnit().getId(),
                unit.getName(),
                unit.getType().name());
    }
}
