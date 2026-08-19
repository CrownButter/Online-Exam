package com.onlineexam.identity.domain.repository;

import com.onlineexam.identity.domain.entity.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByIdAndOrganizationId(Long id, Long organizationId);

    Optional<Role> findByIdAndOrganizationIdIsNull(Long id);

    boolean existsByOrganizationIdAndName(Long organizationId, String name);
}
