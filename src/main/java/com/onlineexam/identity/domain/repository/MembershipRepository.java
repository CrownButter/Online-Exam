package com.onlineexam.identity.domain.repository;

import com.onlineexam.identity.domain.entity.Membership;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    Optional<Membership> findByIdAndOrganization_Id(Long id, Long organizationId);
}
