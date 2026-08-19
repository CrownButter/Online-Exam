# Domain Model Specification V1 — Organization & User Multi-Tenant Identity

## Status

**Step:** 2 — Domain Model Specification  
**Status:** APPROVED  
**Database Contract:** Approved and implemented through Flyway V2  

This document is the schematic/contract for the Java domain model. It intentionally does **not** implement JPA entities yet.

## 1. Purpose

The domain model mirrors the approved database contract while keeping entity navigation intentionally narrow. The model must express domain relationships without automatically creating bidirectional graphs or persistence cascades.

The implementation gate remains:

```text
Database Contract
    ↓
Domain Model Specification  ✅ APPROVED
    ↓
JPA Entity Implementation  ← NEXT STAGE
    ↓
Repository
    ↓
Service
    ↓
DTO
    ↓
REST Controller
```

## 2. Entities

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

## 3. Scalar Types and Enums

All generated database identifiers are represented by:

```java
Long
```

Database timestamps use `DATETIME(6)` and Java uses:

```java
Instant
```

Status/type columns are represented by Java enums and persisted as strings.

```text
OrganizationStatus
  ACTIVE
  SUSPENDED

OrganizationalUnitType
  BRANCH
  DEPARTMENT
  CLASS

UserStatus
  ACTIVE
  INACTIVE
  LOCKED

MembershipStatus
  ACTIVE
  REVOKED
```

Persistence strategy:

```java
@Enumerated(EnumType.STRING)
```

Do not persist enum ordinals.

`OrganizationalUnitType` is intentionally a closed V1 enum. Supporting tenant-defined/custom unit types later would require an explicit schema/domain change and migration rather than dynamic values in this version.

## 4. Organization

```text
Organization
 ├─ id: Long
 ├─ name: String
 ├─ slug: String
 ├─ status: OrganizationStatus
 ├─ createdAt: Instant
 ├─ updatedAt: Instant
 └─ deletedAt: Instant?
```

Relationships:

```text
Organization → none by default
```

The model intentionally does not expose collections such as `users`, `memberships`, `roles`, or `organizationalUnits` on the parent entity unless a later domain use case requires them.

Rationale:
- prevent large object graphs;
- avoid accidental lazy-loading;
- reduce serialization risk;
- preserve aggregate boundaries;
- keep tenant-owned traversal explicit.

Domain behavior in V1:

No helper method is required beyond ordinary state access. Soft-delete state is represented by `deletedAt`.

## 5. OrganizationalUnit

```text
OrganizationalUnit
 ├─ id: Long
 ├─ organization: Organization
 ├─ parentUnit: OrganizationalUnit?
 ├─ name: String
 ├─ type: OrganizationalUnitType
 ├─ createdAt: Instant
 ├─ updatedAt: Instant
 └─ deletedAt: Instant?
```

Relationships:

```text
OrganizationalUnit → Organization
OrganizationalUnit → parentUnit (self-reference, nullable)
```

No child collection is modeled:

```text
children: List<OrganizationalUnit>  ← NOT included in V1
```

Cycle detection is a Service-layer invariant. Entity logic must not query the database.

## 6. AppUser

```text
AppUser
 ├─ id: Long
 ├─ organization: Organization
 ├─ email: String
 ├─ username: String
 ├─ passwordHash: String
 ├─ fullName: String
 ├─ status: UserStatus
 ├─ createdAt: Instant
 ├─ updatedAt: Instant
 └─ deletedAt: Instant?
```

Relationship:

```text
AppUser → Organization
```

No membership collection is modeled in V1.

Tenant-scoped identity remains a database contract rule:

```text
UNIQUE (organization_id, email)
UNIQUE (organization_id, username)
```

## 7. Membership

```text
Membership
 ├─ id: Long
 ├─ organization: Organization
 ├─ user: AppUser
 ├─ organizationalUnit: OrganizationalUnit?
 ├─ status: MembershipStatus
 ├─ createdAt: Instant
 ├─ updatedAt: Instant
 └─ deletedAt: Instant?
```

Relationships:

```text
Membership → Organization
Membership → AppUser
Membership → OrganizationalUnit?
```

### Domain helper

```java
boolean isOrganizationLevel()
```

Definition:

```text
organizationalUnit == null
    → organization-level membership

organizationalUnit != null
    → unit-level membership
```

The method is pure domain logic. It must not perform repository access or other I/O.

### Tenant invariants

The entity itself does not independently query other tables to validate:

```text
membership.organization == user.organization
membership.organization == organizationalUnit.organization
```

These remain Service-layer invariants as defined by the database contract.

## 8. Role

```text
Role
 ├─ id: Long
 ├─ organization: Organization?
 ├─ name: String
 ├─ isSystemRole: boolean
 ├─ createdAt: Instant
 ├─ updatedAt: Instant
 └─ deletedAt: Instant?
```

Relationship:

```text
Role → Organization?
```

### Domain helper

```java
boolean isGlobalRole()
```

Definition:

```text
organization == null
    → global/system role
```

The persisted source of truth remains the approved database pair:

```text
organization_id
is_system_role
```

The entity must not introduce another persisted `global`/`isGlobal` column.

### Role scope consistency

The database already enforces:

```text
isSystemRole = TRUE  -> organization == null
isSystemRole = FALSE -> organization != null
```

The entity should not silently repair invalid combinations. Service creation/update operations will enforce the business rule before persistence.

## 9. Permission

```text
Permission
 ├─ id: Long
 ├─ code: String
 └─ description: String?
```

Permission is system master data.

Relationship:

```text
Permission → none by default
```

No lifecycle timestamps and no soft-delete state are modeled.

## 10. MembershipRole

Mapping entity for:

```text
Membership ──< MembershipRole >── Role
```

Schematic:

```text
MembershipRole
 ├─ id: MembershipRoleId
 ├─ membership: Membership
 └─ role: Role
```

Composite identifier:

```text
MembershipRoleId
 ├─ membershipId: Long
 └─ roleId: Long
```

### ID strategy

Use JPA `@EmbeddedId`.

The mapping entity does not have a surrogate `Long id`.

No soft-delete columns.

Relationships:

```text
MembershipRole → Membership
MembershipRole → Role
```

Do not add convenience collections to `Membership` or `Role` at this stage.

## 11. RolePermission

Mapping entity for:

```text
Role ──< RolePermission >── Permission
```

Schematic:

```text
RolePermission
 ├─ id: RolePermissionId
 ├─ role: Role
 └─ permission: Permission
```

Composite identifier:

```text
RolePermissionId
 ├─ roleId: Long
 └─ permissionId: Long
```

### ID strategy

Use JPA `@EmbeddedId`.

No surrogate `Long id`.

No soft-delete columns.

Relationships:

```text
RolePermission → Role
RolePermission → Permission
```

## 12. Relationship Direction

The intended JPA relationship graph is unidirectional:

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

The following parent-side collections are deliberately excluded in V1:

```text
Organization.users
Organization.memberships
Organization.roles
Organization.organizationalUnits
AppUser.memberships
Membership.roles
Role.memberships
Role.permissions
Permission.roles
```

These can be introduced later when a concrete domain operation requires aggregate navigation in that direction.

## 13. Fetch Strategy

For all entity-to-entity associations in this module, use explicit `LAZY` fetching where JPA permits it.

Target intent:

```text
@ManyToOne(fetch = FetchType.LAZY)
```

for:

```text
AppUser.organization
OrganizationalUnit.organization
OrganizationalUnit.parentUnit
Membership.organization
Membership.user
Membership.organizationalUnit
Role.organization
MembershipRole.membership
MembershipRole.role
RolePermission.role
RolePermission.permission
```

Do not rely on provider defaults for association loading.

Reason:
- keep repository/service queries explicit;
- avoid accidental graph hydration;
- reduce N+1 risk by making access deliberate;
- prevent serialization from traversing large graphs.

## 14. Cascade Strategy

Default for this module:

```text
NO CASCADE
```

In particular:

```text
CascadeType.ALL  ← prohibited by default
```

No entity relationship should implicitly create, update, or delete another domain entity.

Lifecycle is controlled by Service operations and Flyway/database constraints.

The mapping entities are especially intentionally non-cascading because role/permission assignment is an explicit domain operation.

## 15. Soft Delete JPA Strategy

Soft delete applies to:

```text
Organization
OrganizationalUnit
AppUser
Membership
Role
```

The intended Hibernate implementation is conceptually:

```java
@SQLDelete(sql = "UPDATE ... SET deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
```

Important implementation note:

The specification deliberately prefers Hibernate's current restriction mechanism (`@SQLRestriction`) rather than blindly copying the older `@Where` pattern. The exact Spring Boot 4.1 / Hibernate 7.4 annotation behavior will be verified during Entity implementation.

The `@SQLDelete` SQL must match the physical table name and primary-key column exactly.

Soft-delete must not be applied to:

```text
Permission
MembershipRole
RolePermission
```

The entity layer must not add application-level magic that changes `deletedAt` on unrelated entities during association changes.

## 16. Entity Mutability

V1 entities should be mutable enough for JPA but should avoid unrestricted public mutation.

Preferred pattern:

```text
- protected no-args constructor for JPA
- constructor/factory for required creation state
- explicit domain methods for state transitions
- avoid public setters for identifiers and relationships
```

Examples of controlled operations that may later be added:

```text
Organization.suspend()
Organization.activate()
Organization.softDelete()

Membership.revoke()
Membership.assignToUnit(...)

Role.rename(...)
Role.softDelete()
```

These behaviors are not yet implementation requirements; they must be finalized together with aggregate/service design before coding.

Do not make every field writeable just for convenience.

## 17. Equality and Hashing Policy

Because entity identifiers are database-generated, `equals()`/`hashCode()` must not be based on mutable business fields such as:

```text
name
email
username
status
```

The concrete implementation should use a stable JPA-safe identity strategy, to be finalized before coding.

Composite ID classes (`MembershipRoleId`, `RolePermissionId`) must use value equality over their ID fields.

## 18. Column Mapping Expectations

Entity fields must map explicitly to the approved snake_case schema where there is any ambiguity.

Examples:

```text
createdAt  -> created_at
updatedAt  -> updated_at
deletedAt  -> deleted_at
passwordHash -> password_hash
fullName -> full_name
isSystemRole -> is_system_role
```

Table names are fixed by the database contract, especially:

```text
app_user
membership_role
role_permission
```

Do not rename tables to Java class names.

## 19. Database Contract Traceability

The Domain Model must preserve these database facts:

```text
app_user.email       = tenant-scoped uniqueness
app_user.username    = tenant-scoped uniqueness

organization.slug    = globally unique and never reused

membership           = explicit organization + user + optional unit

role                 = global/system or tenant-scoped
permission           = global master data

membership_role      = composite key
role_permission      = composite key
```

No entity-level annotation should contradict the schema contract.

## 20. Concurrency / Optimistic Locking Note

This Identity module intentionally does **not** introduce JPA `@Version` / optimistic locking in V1.

Reason:

```text
Concurrent writes to the same Identity row are not currently a primary contention pattern.
```

This is a deliberate non-blocking decision, not a statement that optimistic locking is unnecessary for the platform as a whole.

Future decision to revisit:

```text
Exam Attempt / Scoring
```

That area is explicitly expected to operate under high concurrency (targeting approximately 1000 CCU) and may require `@Version` or an equivalent concurrency-control strategy to prevent race conditions during concurrent submission, grading, score updates, or state transitions.

Do not add `@Version` to the current Identity entities solely because of this note.

## 21. Explicit Non-Goals for Entity Stage

Do not implement in Step 2/Entity design:

- Spring Data repositories
- authorization services
- authentication
- JWT/session handling
- REST controllers
- DTO mapping
- Redis
- Kafka
- frontend
- cross-tenant query services
- hierarchy traversal repositories
- role/permission administration endpoints

## 22. Approval Decision

The user explicitly approved all nine Domain Model decisions:

1. unidirectional entity relationships;
2. `@EmbeddedId` for mapping entities;
3. `Membership.isOrganizationLevel()`;
4. `Role.isGlobalRole()`;
5. lazy association loading;
6. no default cascades;
7. Hibernate soft-delete strategy using `@SQLDelete` plus the current restriction annotation to be verified during implementation;
8. controlled mutation rather than unrestricted setters;
9. enum persistence with `EnumType.STRING`.

Additional acknowledged design notes:

- `@Version` is not part of Identity V1 and is recorded as a future concurrency decision for Exam Attempt/Scoring.
- `OrganizationalUnitType` remains a closed V1 enum. Custom tenant-defined unit types would require a deliberate schema/domain migration later.

**Entity implementation is now unblocked and is the next stage.**

## 23. Next Stage Gate

Proceed to:

**Step 2 — JPA Entity Implementation**

Implementation must follow this specification exactly and must remain limited to the Domain Model layer. Do not create repositories, services, DTOs, controllers, authentication, or frontend as part of this stage.
