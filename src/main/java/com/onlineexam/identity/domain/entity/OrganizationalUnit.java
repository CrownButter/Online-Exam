package com.onlineexam.identity.domain.entity;

import com.onlineexam.identity.domain.OrganizationalUnitType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "organizational_unit")
@SQLDelete(sql = "UPDATE organizational_unit SET deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class OrganizationalUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_unit_id")
    private OrganizationalUnit parentUnit;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrganizationalUnitType type;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected OrganizationalUnit() {
    }

    public OrganizationalUnit(Organization organization, String name, OrganizationalUnitType type) {
        this.organization = organization;
        this.name = name;
        this.type = type;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Organization getOrganization() {
        return organization;
    }

    public OrganizationalUnit getParentUnit() {
        return parentUnit;
    }

    public String getName() {
        return name;
    }

    public OrganizationalUnitType getType() {
        return type;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void rename(String name) {
        this.name = name;
    }

    public void changeType(OrganizationalUnitType type) {
        this.type = type;
    }

    public void assignParent(OrganizationalUnit parentUnit) {
        this.parentUnit = parentUnit;
    }

    public void removeParent() {
        this.parentUnit = null;
    }
}
