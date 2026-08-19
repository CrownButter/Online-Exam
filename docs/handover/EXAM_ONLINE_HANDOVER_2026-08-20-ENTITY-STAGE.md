# Exam Online — Handover 2026-08-20 — Entity Stage

## Repository

`CrownButter/Online-Exam`

Branch:

`main`

## Current Progress

```text
Database Contract                  ✅ APPROVED / IMPLEMENTED
Flyway V1                          ✅ APPLIED
Flyway V2                          ✅ APPLIED
Database Contract Documentation    ✅ COMMITTED
Domain Model Specification         ✅ APPROVED
JPA Entity Implementation          🟡 NEXT STAGE
Repositories                       ⛔ NOT STARTED
Services                           ⛔ NOT STARTED
DTOs                               ⛔ NOT STARTED
REST Controllers                   ⛔ NOT STARTED
```

## Latest Approval

The user explicitly approved all nine Domain Model decisions:

1. Unidirectional entity relationships.
2. `@EmbeddedId` for `MembershipRole` and `RolePermission`.
3. `Membership.isOrganizationLevel()`.
4. `Role.isGlobalRole()`.
5. Explicit lazy association loading.
6. No default cascade operations.
7. Hibernate soft-delete using `@SQLDelete` plus the current Hibernate restriction annotation, with exact behavior to be verified during implementation.
8. Controlled entity mutation rather than unrestricted public setters.
9. Enum persistence with `EnumType.STRING`.

Additional notes explicitly approved:

- No `@Version` in Identity V1. It is intentionally recorded as a future concurrency-control decision for Exam Attempt/Scoring, where high concurrency and concurrent state changes make optimistic locking more likely to be necessary.
- `OrganizationalUnitType` remains a closed V1 enum: `BRANCH`, `DEPARTMENT`, `CLASS`. Tenant-defined custom unit types would require an explicit future schema/domain migration.

## Authoritative Specification

File:

`docs/domain-model/ORGANIZATION-IDENTITY-V1-DOMAIN-MODEL-SPEC.md`

Latest approval commit:

`236c588b0b0a9ac33adf75b25f98c6d03bae0ca9`

Status in that document is now:

`APPROVED`

## Approved Entity Model

```text
Organization
 ├─ id: Long
 ├─ name: String
 ├─ slug: String
 ├─ status: OrganizationStatus
 ├─ createdAt: Instant
 ├─ updatedAt: Instant
 └─ deletedAt: Instant?

OrganizationalUnit
 ├─ id: Long
 ├─ organization: Organization
 ├─ parentUnit: OrganizationalUnit?
 ├─ name: String
 ├─ type: OrganizationalUnitType
 ├─ createdAt: Instant
 ├─ updatedAt: Instant
 └─ deletedAt: Instant?

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

Membership
 ├─ id: Long
 ├─ organization: Organization
 ├─ user: AppUser
 ├─ organizationalUnit: OrganizationalUnit?
 ├─ status: MembershipStatus
 ├─ createdAt: Instant
 ├─ updatedAt: Instant
 └─ deletedAt: Instant?

Role
 ├─ id: Long
 ├─ organization: Organization?
 ├─ name: String
 ├─ isSystemRole: boolean
 ├─ createdAt: Instant
 ├─ updatedAt: Instant
 └─ deletedAt: Instant?

Permission
 ├─ id: Long
 ├─ code: String
 └─ description: String?

MembershipRole
 ├─ id: MembershipRoleId
 ├─ membership: Membership
 └─ role: Role

MembershipRoleId
 ├─ membershipId: Long
 └─ roleId: Long

RolePermission
 ├─ id: RolePermissionId
 ├─ role: Role
 └─ permission: Permission

RolePermissionId
 ├─ roleId: Long
 └─ permissionId: Long
```

## Relationship Rules

Unidirectional only:

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

No parent-side collections unless a later concrete use case requires them.

## Persistence Rules

Associations:

```java
@ManyToOne(fetch = FetchType.LAZY)
```

Default cascade:

```text
none
```

Do not use `CascadeType.ALL` by default.

Enums:

```java
@Enumerated(EnumType.STRING)
```

Composite IDs:

```java
@EmbeddedId
```

Soft-delete entities:

```text
Organization
OrganizationalUnit
AppUser
Membership
Role
```

Expected Hibernate strategy:

```java
@SQLDelete(...)
@SQLRestriction("deleted_at IS NULL")
```

Exact SQL must match the physical schema and will be checked while implementing entities.

## Domain Helpers

`Membership`:

```java
boolean isOrganizationLevel()
```

Purely evaluates whether `organizationalUnit == null`.

`Role`:

```java
boolean isGlobalRole()
```

Purely evaluates whether `organization == null`.

Neither helper may perform I/O.

## Entity Mutation

Preferred pattern:

- protected no-args constructor for JPA;
- constructor/factory for required creation state;
- controlled domain methods for state transitions;
- avoid public setters for identifiers and relationships.

Concrete mutation methods are to be implemented only where justified by the domain model. Do not invent broad behavior merely to populate setters.

## Equality / Hashing

Generated entity IDs are not business values. Do not base `equals()` / `hashCode()` on mutable fields such as name, email, username, or status.

Use a JPA-safe stable identity strategy for entities. Composite ID classes use value equality across their ID fields.

## Scope Rules For This Stage

The next stage is **JPA Entity Implementation only**.

Do NOT add:

- repositories;
- services;
- DTOs;
- controllers;
- authentication/security;
- JWT/session logic;
- Redis;
- Kafka;
- frontend.

Also do not change Flyway V1/V2 unless an actual schema contradiction is discovered and explicitly addressed as a separate migration decision.

## Concurrency Note

Do not add `@Version` to the current Identity entities.

Future module to revisit:

```text
Exam Attempt / Scoring
```

Expected concern there:

```text
high concurrency
concurrent submission
state transitions
score updates
race-condition prevention
```

An optimistic-locking or equivalent concurrency strategy will be decided when that domain is designed.

## Next Chat Starting Point

Start by inspecting the existing Java source tree and build configuration.

Then implement the approved Identity entities and their supporting enum/composite-ID classes only.

After implementation, verify:

1. compilation;
2. application startup;
3. Hibernate schema validation/mapping compatibility against the existing Flyway schema;
4. relevant tests.

Once Entity stage is complete, stop and wait for explicit approval before creating repositories.
