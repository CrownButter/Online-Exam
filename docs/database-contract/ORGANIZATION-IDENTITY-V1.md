# Database Contract V1 — Organization & User Multi-Tenant Identity

## Status

**Step:** 1 — Database Contract  
**Status:** Implemented in Flyway and committed to `main`  
**Migration:** `src/main/resources/db/migration/V1__create_organization_identity_schema.sql`  
**Commit:** `a3a173b3df5feea2e4bc290ea62a2ae59559af03`

This document records the database contract agreed for the first application domain. It is intentionally limited to the database layer. Domain entities, repositories, services, DTOs, and REST controllers are not part of this step.

## Technology assumptions

- Java 21
- Spring Boot 4.1.x
- Spring Data JPA
- MySQL 8.x
- Flyway
- Maven
- Base package: `com.onlineexam`

## Module scope

The module establishes the tenant and identity backbone for the online examination platform:

```text
Organization
├── OrganizationalUnit
├── app_user
├── Membership
└── Role
    └── Permission
```

Many-to-many mappings:

```text
Membership ──< MembershipRole >── Role
Role       ──< RolePermission >── Permission
```

## Tables

### 1. `organization`

Represents a tenant in the system.

| Column | Type | Null | Constraint / Meaning |
|---|---|---:|---|
| `id` | `BIGINT UNSIGNED` | NO | PK, auto increment |
| `name` | `VARCHAR(150)` | NO | Tenant display name |
| `slug` | `VARCHAR(100)` | NO | Global unique tenant identifier; not reused after soft-delete |
| `status` | `VARCHAR(20)` | NO | `ACTIVE` / `SUSPENDED` |
| `created_at` | `DATETIME(6)` | NO | Creation timestamp |
| `updated_at` | `DATETIME(6)` | NO | Last update timestamp |
| `deleted_at` | `DATETIME(6)` | YES | Soft-delete timestamp |

Important rule: `slug` remains globally unique even when the organization is soft-deleted. This intentionally prevents slug reuse for audit trail and URL/subdomain stability.

### 2. `organizational_unit`

Represents hierarchical organizational structures inside a tenant, such as branch, department, and class.

| Column | Type | Null | Constraint / Meaning |
|---|---|---:|---|
| `id` | `BIGINT UNSIGNED` | NO | PK, auto increment |
| `organization_id` | `BIGINT UNSIGNED` | NO | FK → `organization.id` |
| `parent_unit_id` | `BIGINT UNSIGNED` | YES | FK → `organizational_unit.id` |
| `name` | `VARCHAR(150)` | NO | Unit name |
| `type` | `VARCHAR(30)` | NO | `BRANCH` / `DEPARTMENT` / `CLASS` |
| `created_at` | `DATETIME(6)` | NO | Creation timestamp |
| `updated_at` | `DATETIME(6)` | NO | Last update timestamp |
| `deleted_at` | `DATETIME(6)` | YES | Soft-delete timestamp |

`parent_unit_id` is self-referencing and nullable. Circular hierarchy detection is deliberately a Service-layer responsibility for V1 rather than a database constraint.

### 3. `app_user`

Tenant-scoped application identity.

| Column | Type | Null | Constraint / Meaning |
|---|---|---:|---|
| `id` | `BIGINT UNSIGNED` | NO | PK, auto increment |
| `organization_id` | `BIGINT UNSIGNED` | NO | FK → `organization.id`; tenant discriminator |
| `email` | `VARCHAR(254)` | NO | Globally unique user email |
| `username` | `VARCHAR(100)` | NO | Unique within organization |
| `password_hash` | `VARCHAR(255)` | NO | Password hash only; plaintext password is never stored |
| `full_name` | `VARCHAR(150)` | NO | User display name |
| `status` | `VARCHAR(20)` | NO | `ACTIVE` / `INACTIVE` / `LOCKED` |
| `created_at` | `DATETIME(6)` | NO | Creation timestamp |
| `updated_at` | `DATETIME(6)` | NO | Last update timestamp |
| `deleted_at` | `DATETIME(6)` | YES | Soft-delete timestamp |

`USER` is intentionally not used as the table name. The chosen table name is `app_user`.

Uniqueness rules:

- `email` is globally unique.
- `(organization_id, username)` is unique.
- Soft-deleted email/username values remain reserved in V1 because the constraints are physical unique constraints rather than reusable soft-delete keys.

### 4. `membership`

Represents a user's membership in an organization and, optionally, in a specific organizational unit.

| Column | Type | Null | Constraint / Meaning |
|---|---|---:|---|
| `id` | `BIGINT UNSIGNED` | NO | PK, auto increment |
| `organization_id` | `BIGINT UNSIGNED` | NO | FK → `organization.id` |
| `user_id` | `BIGINT UNSIGNED` | NO | FK → `app_user.id` |
| `organizational_unit_id` | `BIGINT UNSIGNED` | YES | FK → `organizational_unit.id`; nullable for org-level membership |
| `status` | `VARCHAR(20)` | NO | `ACTIVE` / `REVOKED` |
| `created_at` | `DATETIME(6)` | NO | Creation timestamp |
| `updated_at` | `DATETIME(6)` | NO | Last update timestamp |
| `deleted_at` | `DATETIME(6)` | YES | Soft-delete timestamp |

The explicit `organization_id` is retained even though `user_id` and `organizational_unit_id` also reference tenant-owned data. This is intentional for tenant scoping, query design, authorization context, and future enforcement.

### 5. `role`

Represents either a global/system role or a tenant-custom role.

| Column | Type | Null | Constraint / Meaning |
|---|---|---:|---|
| `id` | `BIGINT UNSIGNED` | NO | PK, auto increment |
| `organization_id` | `BIGINT UNSIGNED` | YES | NULL for global/system role; non-null for tenant role |
| `name` | `VARCHAR(100)` | NO | Human-readable role name |
| `is_system_role` | `BOOLEAN` | NO | Scope discriminator |
| `created_at` | `DATETIME(6)` | NO | Creation timestamp |
| `updated_at` | `DATETIME(6)` | NO | Last update timestamp |
| `deleted_at` | `DATETIME(6)` | YES | Soft-delete timestamp |

Database-enforced invariant:

```text
is_system_role = TRUE  -> organization_id IS NULL
is_system_role = FALSE -> organization_id IS NOT NULL
```

This is implemented with a MySQL `CHECK` constraint.

### 6. `permission`

System master data describing fine-grained authorization capabilities.

| Column | Type | Null | Constraint / Meaning |
|---|---|---:|---|
| `id` | `BIGINT UNSIGNED` | NO | PK, auto increment |
| `code` | `VARCHAR(100)` | NO | Globally unique permission code |
| `description` | `VARCHAR(255)` | YES | Human-readable description |

Permission does not have `created_at`, `updated_at`, or `deleted_at` in V1. It is treated as system master data rather than tenant lifecycle data.

### 7. `membership_role`

Maps memberships to roles.

Composite primary key:

```text
(membership_id, role_id)
```

No surrogate `id` is used.

### 8. `role_permission`

Maps roles to permissions.

Composite primary key:

```text
(role_id, permission_id)
```

No surrogate `id` is used.

## Referential integrity

All foreign keys use:

```text
ON DELETE RESTRICT
ON UPDATE RESTRICT
```

No domain data is cascade-deleted. Application-level soft-delete is the intended lifecycle mechanism.

## Soft-delete convention

Soft-delete applies to:

- `organization`
- `organizational_unit`
- `app_user`
- `membership`
- `role`

Active rows have:

```sql
deleted_at IS NULL
```

Deleted rows have a non-null `deleted_at` timestamp.

`permission`, `membership_role`, and `role_permission` do not have `deleted_at` in V1.

## Time convention

All application timestamps use:

```text
DATETIME(6)
```

The application contract is UTC and Java will use `Instant` for the corresponding time values.

This is intentional because the examination domain will later depend heavily on exact timestamps for scheduling, attempts, deadlines, submissions, and grading.

## Tenant consistency rules

The following invariants are intentionally not encoded as composite foreign keys in V1:

```text
membership.organization_id
    == app_user.organization_id
```

```text
membership.organization_id
    == organizational_unit.organization_id
```

For tenant roles:

```text
membership.organization_id
    == role.organization_id
```

These relationships remain protected by explicit foreign keys for referential existence, while cross-tenant consistency is an application Service-layer responsibility in V1.

The schema is indexed to support tenant-scoped access patterns.

## Organizational hierarchy rule

`organizational_unit.parent_unit_id` supports nested structures such as:

```text
Organization
└── Branch / Campus
    └── Department / Faculty
        └── Class
```

The database guarantees that the parent row exists when referenced. It does not attempt to detect cycles such as:

```text
A -> B -> C -> A
```

Cycle prevention belongs to the Service layer in V1.

## Role-name uniqueness

V1 does not currently enforce a database-level uniqueness rule for `role.name`.

The intended business rule is:

- global/system role names are unique among global/system roles;
- tenant-custom role names are unique within the tenant.

This is currently an application-level invariant. The schema intentionally does not introduce a generated scope column solely for this rule before the business semantics are further validated.

## Indexing strategy

Indexes are provided for the main tenant-scoped and relationship lookup paths:

- organization status and soft-delete
- organizational unit organization and parent hierarchy
- user organization and status
- membership organization, user, organizational unit, and status
- role organization and system-role scope
- reverse lookup on both many-to-many mappings

The schema is designed to support repository queries that always scope tenant-owned records explicitly.

## What was implemented

This step implemented only the database contract:

1. Created Flyway V1 migration.
2. Created all eight tables for the organization/identity backbone.
3. Added primary keys and foreign keys.
4. Added status/type `CHECK` constraints.
5. Added role system-vs-tenant scope `CHECK` constraint.
6. Added global and tenant-scoped uniqueness rules agreed for V1.
7. Added soft-delete columns to tenant/domain entities.
8. Added UTC microsecond timestamp columns.
9. Added indexes for tenant-scoped access and relationship traversal.
10. Prevented cascading deletes with explicit `RESTRICT` actions.

## Deliberately not implemented yet

The following are outside Step 1 and must not be added until the Database Contract is approved:

- JPA entities
- `@SQLDelete`
- `@Where`
- Spring Data repositories
- service/business logic
- DTOs
- REST controllers
- authentication/security configuration
- Redis/Kafka
- frontend

## Next step gate

Do not proceed to Domain Model until this V1 database contract has been reviewed and explicitly approved.
