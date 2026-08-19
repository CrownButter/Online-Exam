package com.onlineexam.identity.application.service;

import com.onlineexam.identity.domain.entity.OrganizationalUnit;
import com.onlineexam.identity.domain.repository.OrganizationalUnitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrganizationalUnitService {

    private final OrganizationalUnitRepository organizationalUnitRepository;

    public OrganizationalUnitService(OrganizationalUnitRepository organizationalUnitRepository) {
        this.organizationalUnitRepository = organizationalUnitRepository;
    }

    @Transactional(readOnly = true)
    public OrganizationalUnit get(Long organizationId, Long unitId) {
        return organizationalUnitRepository.findByIdAndOrganization_Id(unitId, organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Organizational unit not found: " + unitId));
    }
}
