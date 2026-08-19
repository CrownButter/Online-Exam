package com.onlineexam.identity.application.service;

import com.onlineexam.identity.domain.entity.Membership;
import com.onlineexam.identity.domain.repository.MembershipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MembershipService {

    private final MembershipRepository membershipRepository;

    public MembershipService(MembershipRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }

    @Transactional(readOnly = true)
    public Membership get(Long organizationId, Long membershipId) {
        return membershipRepository.findByIdAndOrganization_Id(membershipId, organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Membership not found: " + membershipId));
    }
}
