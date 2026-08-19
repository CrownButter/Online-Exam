package com.onlineexam.identity.domain.repository;

import com.onlineexam.identity.domain.entity.OrganizationalUnit;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationalUnitRepository extends JpaRepository<OrganizationalUnit, Long> {

    Optional<OrganizationalUnit> findByIdAndOrganizationId(Long id, Long organizationId);
}
