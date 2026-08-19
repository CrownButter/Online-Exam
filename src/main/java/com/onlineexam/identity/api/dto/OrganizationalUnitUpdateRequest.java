package com.onlineexam.identity.api.dto;

import com.onlineexam.identity.domain.OrganizationalUnitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrganizationalUnitUpdateRequest(
        @NotBlank @Size(max = 150) String name,
        @NotNull OrganizationalUnitType type,
        Long parentUnitId) {
}
