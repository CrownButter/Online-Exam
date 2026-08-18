-- ============================================================
-- V1__create_organization_identity_schema.sql
-- ============================================================
-- Module:
--   Organization & User Multi-Tenant Identity
--
-- Database:
--   MySQL 8.x
--
-- Conventions:
--   - Primary keys: BIGINT UNSIGNED AUTO_INCREMENT
--   - Timestamps: DATETIME(6), application/database UTC
--   - Soft delete: deleted_at IS NULL means active
--   - No cascading deletes for domain data
--   - Tenant consistency beyond FK existence is enforced
--     by the application service layer in V1.
-- ============================================================


-- ============================================================
-- 1. ORGANIZATION
-- ============================================================

CREATE TABLE organization (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,

    name VARCHAR(150) NOT NULL,

    slug VARCHAR(100) NOT NULL,

    status VARCHAR(20) NOT NULL,

    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    deleted_at DATETIME(6) NULL,

    CONSTRAINT pk_organization
        PRIMARY KEY (id),

    CONSTRAINT uq_organization_slug
        UNIQUE (slug),

    CONSTRAINT ck_organization_status
        CHECK (status IN ('ACTIVE', 'SUSPENDED')),

    INDEX idx_organization_status
        (status),

    INDEX idx_organization_deleted_at
        (deleted_at)
);


-- ============================================================
-- 2. ORGANIZATIONAL UNIT
-- ============================================================

CREATE TABLE organizational_unit (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,

    organization_id BIGINT UNSIGNED NOT NULL,

    parent_unit_id BIGINT UNSIGNED NULL,

    name VARCHAR(150) NOT NULL,

    type VARCHAR(30) NOT NULL,

    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    deleted_at DATETIME(6) NULL,

    CONSTRAINT pk_organizational_unit
        PRIMARY KEY (id),

    CONSTRAINT fk_organizational_unit_organization
        FOREIGN KEY (organization_id)
        REFERENCES organization (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT fk_organizational_unit_parent
        FOREIGN KEY (parent_unit_id)
        REFERENCES organizational_unit (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT ck_organizational_unit_type
        CHECK (
            type IN (
                'BRANCH',
                'DEPARTMENT',
                'CLASS'
            )
        ),

    INDEX idx_organizational_unit_organization
        (organization_id),

    INDEX idx_organizational_unit_parent
        (parent_unit_id),

    INDEX idx_organizational_unit_organization_parent
        (organization_id, parent_unit_id),

    INDEX idx_organizational_unit_deleted_at
        (deleted_at)
);


-- ============================================================
-- 3. APP USER
-- ============================================================

CREATE TABLE app_user (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,

    organization_id BIGINT UNSIGNED NOT NULL,

    email VARCHAR(254) NOT NULL,

    username VARCHAR(100) NOT NULL,

    password_hash VARCHAR(255) NOT NULL,

    full_name VARCHAR(150) NOT NULL,

    status VARCHAR(20) NOT NULL,

    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    deleted_at DATETIME(6) NULL,

    CONSTRAINT pk_app_user
        PRIMARY KEY (id),

    CONSTRAINT fk_app_user_organization
        FOREIGN KEY (organization_id)
        REFERENCES organization (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT uq_app_user_email
        UNIQUE (email),

    CONSTRAINT uq_app_user_organization_username
        UNIQUE (organization_id, username),

    CONSTRAINT ck_app_user_status
        CHECK (
            status IN (
                'ACTIVE',
                'INACTIVE',
                'LOCKED'
            )
        ),

    INDEX idx_app_user_organization
        (organization_id),

    INDEX idx_app_user_organization_status
        (organization_id, status),

    INDEX idx_app_user_deleted_at
        (deleted_at)
);


-- ============================================================
-- 4. MEMBERSHIP
-- ============================================================

CREATE TABLE membership (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,

    organization_id BIGINT UNSIGNED NOT NULL,

    user_id BIGINT UNSIGNED NOT NULL,

    organizational_unit_id BIGINT UNSIGNED NULL,

    status VARCHAR(20) NOT NULL,

    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    deleted_at DATETIME(6) NULL,

    CONSTRAINT pk_membership
        PRIMARY KEY (id),

    CONSTRAINT fk_membership_organization
        FOREIGN KEY (organization_id)
        REFERENCES organization (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT fk_membership_user
        FOREIGN KEY (user_id)
        REFERENCES app_user (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT fk_membership_organizational_unit
        FOREIGN KEY (organizational_unit_id)
        REFERENCES organizational_unit (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT ck_membership_status
        CHECK (
            status IN (
                'ACTIVE',
                'REVOKED'
            )
        ),

    INDEX idx_membership_organization
        (organization_id),

    INDEX idx_membership_user
        (user_id),

    INDEX idx_membership_organization_user
        (organization_id, user_id),

    INDEX idx_membership_organizational_unit
        (organizational_unit_id),

    INDEX idx_membership_status
        (organization_id, status),

    INDEX idx_membership_deleted_at
        (deleted_at)
);


-- ============================================================
-- 5. ROLE
-- ============================================================

CREATE TABLE role (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,

    organization_id BIGINT UNSIGNED NULL,

    name VARCHAR(100) NOT NULL,

    is_system_role BOOLEAN NOT NULL DEFAULT FALSE,

    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    deleted_at DATETIME(6) NULL,

    CONSTRAINT pk_role
        PRIMARY KEY (id),

    CONSTRAINT fk_role_organization
        FOREIGN KEY (organization_id)
        REFERENCES organization (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT ck_role_system_scope
        CHECK (
            (
                is_system_role = TRUE
                AND organization_id IS NULL
            )
            OR
            (
                is_system_role = FALSE
                AND organization_id IS NOT NULL
            )
        ),

    INDEX idx_role_organization
        (organization_id),

    INDEX idx_role_organization_system
        (organization_id, is_system_role),

    INDEX idx_role_deleted_at
        (deleted_at)
);


-- ============================================================
-- 6. PERMISSION
-- ============================================================

CREATE TABLE permission (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,

    code VARCHAR(100) NOT NULL,

    description VARCHAR(255) NULL,

    CONSTRAINT pk_permission
        PRIMARY KEY (id),

    CONSTRAINT uq_permission_code
        UNIQUE (code)
);


-- ============================================================
-- 7. MEMBERSHIP ROLE
-- ============================================================

CREATE TABLE membership_role (
    membership_id BIGINT UNSIGNED NOT NULL,

    role_id BIGINT UNSIGNED NOT NULL,

    CONSTRAINT pk_membership_role
        PRIMARY KEY (membership_id, role_id),

    CONSTRAINT fk_membership_role_membership
        FOREIGN KEY (membership_id)
        REFERENCES membership (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT fk_membership_role_role
        FOREIGN KEY (role_id)
        REFERENCES role (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    INDEX idx_membership_role_role
        (role_id)
);


-- ============================================================
-- 8. ROLE PERMISSION
-- ============================================================

CREATE TABLE role_permission (
    role_id BIGINT UNSIGNED NOT NULL,

    permission_id BIGINT UNSIGNED NOT NULL,

    CONSTRAINT pk_role_permission
        PRIMARY KEY (role_id, permission_id),

    CONSTRAINT fk_role_permission_role
        FOREIGN KEY (role_id)
        REFERENCES role (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT fk_role_permission_permission
        FOREIGN KEY (permission_id)
        REFERENCES permission (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    INDEX idx_role_permission_permission
        (permission_id)
);
