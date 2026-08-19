package com.onlineexam.identity.application.service;

import com.onlineexam.identity.domain.entity.Organization;
import com.onlineexam.identity.domain.entity.Role;
import com.onlineexam.identity.domain.repository.OrganizationRepository;
import com.onlineexam.identity.domain.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;

    public RoleService(RoleRepository roleRepository, OrganizationRepository organizationRepository) {
        this.roleRepository = roleRepository;
        this.organizationRepository = organizationRepository;
    }

    @Transactional(readOnly = true)
    public Role getTenantRole(Long organizationId, Long roleId) {
        return roleRepository.findByIdAndOrganization_Id(roleId, organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
    }

    @Transactional(readOnly = true)
    public Role getGlobalRole(Long roleId) {
        return roleRepository.findByIdAndOrganizationIsNull(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Global role not found: " + roleId));
    }

    public Role createTenantRole(Long organizationId, String name) {
        if (roleRepository.existsByOrganization_IdAndName(organizationId, name)) {
            throw new IllegalArgumentException("Role already exists: " + name);
        }
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found: " + organizationId));
        return roleRepository.save(Role.tenant(organization, name));
    }
}
