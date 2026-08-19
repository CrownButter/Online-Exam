package com.onlineexam.identity.domain.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "membership_role")
public class MembershipRole {

    @EmbeddedId
    private MembershipRoleId id = new MembershipRoleId(null, null);

    @MapsId("membershipId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membership_id", nullable = false)
    private Membership membership;

    @MapsId("roleId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    protected MembershipRole() {
    }

    public MembershipRole(Membership membership, Role role) {
        this.membership = membership;
        this.role = role;
    }

    public MembershipRoleId getId() {
        return id;
    }

    public Membership getMembership() {
        return membership;
    }

    public Role getRole() {
        return role;
    }
}
