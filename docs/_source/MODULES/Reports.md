# Reports

## 1. Feature Overview

Operational and regulatory reporting. **Not implemented** --- empty
`com.bankone.report`; sidebar item has no `routerLink`.

**Status:** Stub / Planned

## 2. Business Purpose

Summaries for management (deposits today, account status mix, customer
growth).

## 3. User Workflow

Planned: Sidebar → Reports → choose report → date filters → export.

## 4. Execution Flow

Planned: `ReportController` → `ReportService` aggregating repositories /
SQL views.

## 5. Database Tables

None dedicated; will read `account`, `customers`, future `transaction`.

## 6. REST APIs

None

## 7--13. Controllers / Services / Repositories / DTOs / Entities / Utils / Config

None

## 14. Validation Rules

Planned: date range limits

## 15. Security Rules

Planned: MANAGER/ADMIN (and AUDITOR when activated)

## 16--18. Exception / Logging / Audit

N/A

## 19. Testing Strategy

When built: compare report totals to known deposit fixtures

## 20. Future Extension Guide

Block on Transaction ledger for money reports; until then only
master-data counts (overlap with Dashboard).

------------------------------------------------------------------------

# Future Modification Guide

### Requirement: Add "accounts by status" report

  ------------------------------------------------------------
  Item                      Detail
  ------------------------- ----------------------------------
  Files                     New `com/bankone/report/**`;
                            `SecurityConfig`; Angular
                            `features/reports`; sidebar
                            `routerLink`

  Classes                   `ReportController`,
                            `ReportService`,
                            `AccountRepository` (query/spec)

  Methods                   e.g. `accountsByStatus()`

  Impact                    Dashboard may reuse same service
                            method
  ------------------------------------------------------------

### Call hierarchy (target)

    ReportController.accountsByStatus()
            ↓
    ReportService.accountsByStatus()
            ↓
    AccountRepository (group by status)
