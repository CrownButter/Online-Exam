package com.onlineexam.identity.domain.repository;

import com.onlineexam.identity.domain.entity.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByIdAndOrganization_Id(Long id, Long organizationId);

    Optional<Role> findByIdAndOrganizationIsNull(Long id);

    boolean existsByOrganization_IdAndName(Long organizationId, String name);
}
