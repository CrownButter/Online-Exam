package com.onlineexam.identity.domain.repository;

import com.onlineexam.identity.domain.entity.AppUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByIdAndOrganizationId(Long id, Long organizationId);

    Optional<AppUser> findByOrganizationIdAndEmail(Long organizationId, String email);

    Optional<AppUser> findByOrganizationIdAndUsername(Long organizationId, String username);

    boolean existsByOrganizationIdAndEmail(Long organizationId, String email);

    boolean existsByOrganizationIdAndUsername(Long organizationId, String username);
}
