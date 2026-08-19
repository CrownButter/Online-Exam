# Exam Online — Handover / Change Log

## Date

2026-08-20

## Repository

`CrownButter/Online-Exam`

Branch:

`main`

Current repository HEAD after this handover update:

`b0e0a1a3ae1be1bc6cce1183d1d2dd1d04e77a91`

## Baseline

The project is a Java 21 / Spring Boot 4.1.x / Spring Data JPA / Hibernate 7.4.x / MySQL 8.x / Flyway application.

The first module is:

`Organization & User Multi-Tenant Identity`

The development workflow remains strictly staged:

```text
Database Contract
    ↓
Domain Model Specification
    ↓
JPA Entity
    ↓
Repository
    ↓
Service
    ↓
DTO
    ↓
REST Controller
```

The next stage cannot start until the current stage is explicitly approved.

## Work already completed before this change

### Database Contract

Approved and implemented:

- Flyway V1 creates the organization/identity schema.
- Flyway V2 corrects `app_user.email` uniqueness from global to tenant-scoped.
- Development database reached Flyway version V2 successfully.
- `app_user` now uses:

```text
UNIQUE (organization_id, email)
UNIQUE (organization_id, username)
```

The original V1 migration is intentionally immutable; V2 is the forward correction.

The committed database contract is:

`docs/database-contract/ORGANIZATION-IDENTITY-V1.md`

### Database schema scope

The module contains:

```text
organization
organizational_unit
app_user
membership
role
permission
membership_role
role_permission
```

Soft delete is used for:

```text
organization
organizational_unit
app_user
membership
role
```

No soft delete exists on:

```text
permission
membership_role
role_permission
```

Foreign keys use restrictive delete/update behavior. Cross-tenant consistency is intentionally a Service-layer invariant for V1.

## Change made in this chat

The repository previously stopped between Database Contract and Java Entity implementation because the user required a schematic-first approach.

A new domain model contract was added:

`docs/domain-model/ORGANIZATION-IDENTITY-V1-DOMAIN-MODEL-SPEC.md`

Commit:

`b0e0a1a3ae1be1bc6cce1183d1d2dd1d04e77a91`

Commit message:

`docs: add organization identity domain model specification`

## Domain Model decisions currently captured

### Entity model

```text
Organization
OrganizationalUnit
AppUser
Membership
Role
Permission
MembershipRole
RolePermission
```

### Relationship direction

Relationships are intentionally unidirectional:

```text
AppUser → Organization

OrganizationalUnit → Organization
OrganizationalUnit → parentUnit

Membership → Organization
Membership → AppUser
Membership → OrganizationalUnit

Role → Organization

MembershipRole → Membership
MembershipRole → Role

RolePermission → Role
RolePermission → Permission
```

Parent-side collections are not introduced without a demonstrated domain requirement.

### Composite IDs

Mapping tables use composite identifiers:

```text
MembershipRoleId
  membershipId
  roleId

RolePermissionId
  roleId
  permissionId
```

The proposed JPA strategy is `@EmbeddedId`.

### Domain helper methods

`Membership.isOrganizationLevel()`:

```text
organizationalUnit == null
```

means organization-level membership.

`Role.isGlobalRole()`:

```text
organization == null
```

means global/system role.

These helpers are pure domain logic and must not access repositories.

### Fetch strategy

Entity associations are intended to be explicitly lazy where supported:

```text
@ManyToOne(fetch = FetchType.LAZY)
```

### Cascade strategy

No default cascade. In particular, do not use `CascadeType.ALL` merely for convenience.

Domain lifecycle remains Service-controlled.

### Enum strategy

Persist enums as strings:

```text
@Enumerated(EnumType.STRING)
```

Never persist enum ordinals.

### Entity mutation

The intended model uses:

- protected JPA no-args constructors;
- creation constructors/factories for required state;
- controlled domain methods for state transitions;
- no unrestricted public setters for identifiers and relationships.

### Soft delete

The schematic intentionally avoids blindly assuming the older Hibernate `@Where` pattern.

The current implementation direction is:

```text
@SQLDelete
@SQLRestriction
```

with the exact Hibernate 7.4 annotation behavior to be verified before Entity code is created.

`deleted_at` applies only to the five soft-deletable domain entities.

## Current approval state

Database Contract:

`APPROVED / IMPLEMENTED`

Domain Model Specification:

`DRAFT / AWAITING EXPLICIT USER APPROVAL`

JPA Entities:

`NOT STARTED`

Repositories:

`NOT STARTED`

Services:

`NOT STARTED`

DTOs:

`NOT STARTED`

Controllers:

`NOT STARTED`

## Important guardrails for the next chat

Do not jump directly to entity implementation until the user approves the domain model specification.

Do not:

- rewrite Flyway V1;
- edit `flyway_schema_history` manually;
- restore global email uniqueness;
- rename `app_user` to `user`;
- add bidirectional entity collections without a use case;
- add `CascadeType.ALL` blindly;
- add repository/service/controller code during the Domain Model review;
- introduce Redis/Kafka/frontend at this stage;
- add soft delete to permissions or mapping tables.

## Next task

Review and finalize:

1. unidirectional relationships;
2. `@EmbeddedId` mapping IDs;
3. `Membership.isOrganizationLevel()`;
4. `Role.isGlobalRole()`;
5. explicit lazy loading;
6. no default cascade;
7. Hibernate 7.4 soft-delete annotation strategy;
8. controlled entity mutation;
9. `EnumType.STRING`.

After explicit approval, implement only the JPA Entity stage, then stop for review before moving to Repository.

## Source of truth

For database decisions, use:

`docs/database-contract/ORGANIZATION-IDENTITY-V1.md`

For the current Java-domain schematic, use:

`docs/domain-model/ORGANIZATION-IDENTITY-V1-DOMAIN-MODEL-SPEC.md`

This file exists so the next chat can recover the exact project state without relying on conversational memory.
