-- ============================================================
-- V2__scope_app_user_email_to_organization.sql
-- ============================================================
-- Module:
--   Organization & User Multi-Tenant Identity
--
-- Purpose:
--   Correct app_user email uniqueness to match the tenant-scoped
--   identity model.
--
-- Decision:
--   email is unique within an organization, not globally.
--
-- Rationale:
--   The same real-world person may have separate app_user rows
--   in different organizations. Therefore the same email address
--   must be allowed across different tenants while remaining
--   unique inside a tenant.
-- ============================================================

ALTER TABLE app_user
    DROP INDEX uq_app_user_email,
    ADD CONSTRAINT uq_app_user_organization_email
        UNIQUE (organization_id, email);
