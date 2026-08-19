package com.onlineexam.identity.domain.repository;

import com.onlineexam.identity.domain.entity.AppUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByIdAndOrganization_Id(Long id, Long organizationId);

    Optional<AppUser> findByOrganization_IdAndEmail(Long organizationId, String email);

    Optional<AppUser> findByOrganization_IdAndUsername(Long organizationId, String username);

    boolean existsByOrganization_IdAndEmail(Long organizationId, String email);

    boolean existsByOrganization_IdAndUsername(Long organizationId, String username);
}
