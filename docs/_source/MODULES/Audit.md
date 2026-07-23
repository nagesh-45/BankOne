# Audit

## 1. Feature Overview

Business/security audit trail API and UI are **not** implemented
(`com.bankone.audit` empty). Partial infra: JPA auditing via
`AuditableEntity` + `JpaAuditingConfig` on some entities;
Account/Customer use ad-hoc timestamp columns.

**Status:** Partial infrastructure / Stub product

## 2. Business Purpose

Answer who changed what and when for compliance and dispute resolution.

## 3. User Workflow

Planned: AUDITOR/ADMIN views audit log filtered by entity, user, date.

## 4. Execution Flow

Today: Spring Data auditing fills `created_by` / `updated_by` where
`AuditableEntity` is used.

Planned:

    AnyService.mutatingOperation()
            ↓
    AuditService.record(event)
            ↓
    AuditRepository.save()

## 5. Database Tables

No `audit_event` table. Auditing columns on `users`, `roles`,
`user_roles`, `account_policy`.

## 6. REST APIs

None

## 7--13. Controllers / Services / Repositories / DTOs / Entities / Utils / Config

- Present: `AuditableEntity`, `JpaAuditingConfig` (common)
- Missing: audit module classes

## 14. Validation Rules

N/A

## 15. Security Rules

Planned: AUDITOR + ADMIN read; never public

## 16. Exception Handling

N/A

## 17. Logging

App logs ≠ audit trail (logs can rotate/lose data)

## 18. Audit Events

Planned event types: LOGIN_SUCCESS/FAIL, CUSTOMER_CREATE, ACCOUNT_OPEN,
DEPOSIT, STATUS_CHANGE, POLICY_CHANGE, USER_UPDATE

## 19. Testing Strategy

When built: mutate entity → audit row; login fail → audit row

## 20. Future Extension Guide

Prefer explicit `AuditService` calls for financial events; keep JPA
auditing for simple entity stamps.

------------------------------------------------------------------------

# Future Modification Guide

### Requirement: Log every deposit

  ----------------------------------------------------------------------
  Item                           Detail
  ------------------------------ ---------------------------------------
  Files                          New `com/bankone/audit/**`;
                                 `AccountServiceImpl.java`

  Classes                        `AuditService`, `AccountServiceImpl`

  Methods                        `deposit()` →
                                 `auditService.record("DEPOSIT", ...)`

  Impact                         Performance; storage; Reports overlap
                                 with Transaction
  ----------------------------------------------------------------------

### Requirement: Expose audit API

  ------------------------------------------------------------
  Item                      Detail
  ------------------------- ----------------------------------
  Files                     `AuditController`,
                            `SecurityConfig` (AUDITOR),
                            Angular reports/audit UI

  Impact                    Activate AUDITOR role currently
                            seeded but unused
  ------------------------------------------------------------

### Call hierarchy (target)

    AccountServiceImpl.deposit()
            ↓
    AuditService.log()
            ↓
    AuditRepository.save()
