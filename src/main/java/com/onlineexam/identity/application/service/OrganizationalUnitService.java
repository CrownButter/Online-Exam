package com.onlineexam.identity.application.service;

import com.onlineexam.identity.domain.OrganizationalUnitType;
import com.onlineexam.identity.domain.entity.OrganizationalUnit;
import com.onlineexam.identity.domain.entity.Organization;
import com.onlineexam.identity.domain.repository.OrganizationalUnitRepository;
import com.onlineexam.identity.domain.repository.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrganizationalUnitService {

    private final OrganizationalUnitRepository organizationalUnitRepository;
    private final OrganizationRepository organizationRepository;

    public OrganizationalUnitService(OrganizationalUnitRepository organizationalUnitRepository,
                                      OrganizationRepository organizationRepository) {
        this.organizationalUnitRepository = organizationalUnitRepository;
        this.organizationRepository = organizationRepository;
    }

    public OrganizationalUnit create(Long organizationId, String name, OrganizationalUnitType type, Long parentUnitId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found: " + organizationId));

        OrganizationalUnit unit = new OrganizationalUnit(organization, name, type);
        if (parentUnitId != null) {
            OrganizationalUnit parent = get(organizationId, parentUnitId);
            unit.assignParent(parent);
        }
        return organizationalUnitRepository.save(unit);
    }

    @Transactional(readOnly = true)
    public OrganizationalUnit get(Long organizationId, Long unitId) {
        return organizationalUnitRepository.findByIdAndOrganization_Id(unitId, organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Organizational unit not found: " + unitId));
    }

    public OrganizationalUnit update(Long organizationId, Long unitId, String name,
                                     OrganizationalUnitType type, Long parentUnitId) {
        OrganizationalUnit unit = get(organizationId, unitId);
        unit.rename(name);
        unit.changeType(type);

        if (parentUnitId == null) {
            unit.removeParent();
        } else {
            if (unitId.equals(parentUnitId)) {
                throw new IllegalArgumentException("Organizational unit cannot be its own parent: " + unitId);
            }
            unit.assignParent(get(organizationId, parentUnitId));
        }
        return unit;
    }

    public void delete(Long organizationId, Long unitId) {
        OrganizationalUnit unit = get(organizationId, unitId);
        organizationalUnitRepository.delete(unit);
    }
}
