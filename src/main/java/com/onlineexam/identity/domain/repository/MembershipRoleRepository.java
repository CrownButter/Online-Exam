package com.onlineexam.identity.domain.repository;

import com.onlineexam.identity.domain.entity.MembershipRole;
import com.onlineexam.identity.domain.entity.MembershipRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRoleRepository extends JpaRepository<MembershipRole, MembershipRoleId> {

    boolean existsByMembershipIdAndRoleId(Long membershipId, Long roleId);
}
