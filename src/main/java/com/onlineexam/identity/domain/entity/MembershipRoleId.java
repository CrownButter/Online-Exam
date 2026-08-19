package com.onlineexam.identity.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class MembershipRoleId implements Serializable {

    @Column(name = "membership_id", nullable = false)
    private Long membershipId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    protected MembershipRoleId() {
    }

    public MembershipRoleId(Long membershipId, Long roleId) {
        this.membershipId = membershipId;
        this.roleId = roleId;
    }

    public Long getMembershipId() {
        return membershipId;
    }

    public Long getRoleId() {
        return roleId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MembershipRoleId that)) {
            return false;
        }
        return Objects.equals(membershipId, that.membershipId)
                && Objects.equals(roleId, that.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(membershipId, roleId);
    }
}
