# Exam Online Handover — NotFound Handling Fix

Date: 2026-08-20

## Context

Postman CRUD testing exposed an error after soft deletion of an `OrganizationalUnit`.

The repository correctly excludes deleted units using `deleted_at IS NULL`. Consequently, a GET after DELETE returns an empty Optional. The service then threw `IllegalArgumentException`, which Spring treated as an unhandled exception and returned HTTP 500.

The intended API behavior is HTTP 404 when an Organization or Organizational Unit cannot be found in the active dataset.

## Root Cause

`OrganizationService` was already using the shared exception type:

`com.onlineexam.shared.api.NotFoundException`

The `OrganizationalUnitService`, however, was still throwing `IllegalArgumentException` for both missing organizations and missing organizational units.

There was also a duplicate identity-local `NotFoundException` class under:

`com.onlineexam.identity.api.exception.NotFoundException`

This duplicate was removed so the Identity module uses the shared application-level exception consistently.

## Changes

### 1. OrganizationalUnitService

Changed missing-resource handling from:

`IllegalArgumentException`

to:

`com.onlineexam.shared.api.NotFoundException`

Affected cases:

- Organization not found during unit creation.
- Organizational unit not found during GET.
- Organizational unit not found during UPDATE.
- Organizational unit not found during DELETE.
- Parent organizational unit not found during CREATE/UPDATE.

### 2. Duplicate exception removed

Removed:

`src/main/java/com/onlineexam/identity/api/exception/NotFoundException.java`

The canonical exception is now:

`src/main/java/com/onlineexam/shared/api/NotFoundException.java`

### 3. Global exception handler

No new handler was required. The repository already contains `ApiExceptionHandler`, which maps the shared `NotFoundException` to HTTP 404.

Expected response shape:

```json
{
  "timestamp": "...",
  "status": 404,
  "error": "Not Found",
  "message": "Organizational unit not found: 1"
}
```

## Test Result To Perform

Restart the application after pulling the latest `main` changes and repeat:

1. GET an existing Organization — 200.
2. GET a non-existing Organization — 404.
3. GET an existing Organizational Unit — 200.
4. GET a non-existing Organizational Unit — 404.
5. DELETE an existing Organizational Unit — 204.
6. GET the deleted Organizational Unit — 404.
7. PUT a deleted/non-existing Organizational Unit — 404.
8. DELETE a deleted/non-existing Organizational Unit — 404.
9. Create a Unit with a non-existing Organization — 404.
10. Create/Update a Unit with a non-existing parent Unit — 404.

## Important Existing Behavior

The SQL observed during the failing GET confirms the repository query includes:

`deleted_at IS NULL`

and filters by both `id` and `organization_id`.

Therefore the soft-delete behavior itself is correct. The fix is strictly at the application exception mapping layer; it does not change the database contract or soft-delete semantics.

## GitHub Commits

- `f6381f6a92200fbe388753d0833ae42f1f2f83a0` — use shared `NotFoundException` in `OrganizationalUnitService`.
- `b5804724f279fc8bdca50a928cd1a0666090469a` — remove duplicate Identity `NotFoundException`.
