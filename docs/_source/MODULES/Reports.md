# Reports

## 1. Feature Overview

Staff **Reports** hub with Chart.js UI and OpenPDF downloads:

- Transaction trends (credits / debits over a date range)
- Account mix (counts by account type / status)
- Transfer approvals summary (ADMIN / MANAGER / AUDITOR)

**Status:** Implemented

## 2. Business Purpose

Management visibility without raw SQL: trends, portfolio mix, approval
workload.

## 3. User Workflow

1. Sidebar → **Reports** (roles with `ACCOUNTS_READ`; approvals category
   restricted to Admin/Manager/Auditor).
2. Choose category → set filters → view chart + on-screen table.
3. Download PDF.

## 4. Execution Flow

    Reports (Angular)
            ↓
    GET /reports/transaction-trends | account-mix | approvals
            ↓
    ReportService aggregations (JPA / native queries)
            ↓
    JSON for charts; /pdf endpoints → OpenPDF bytes

## 5. Database Tables

No dedicated report tables. Reads `bank_transaction`, `account`,
`transfer_request`.

## 6. REST APIs

Base: `/reports`

  -----------------------------------------------------------------------
  Method   Path                              Access
  -------- --------------------------------- ----------------------------
  GET      `/reports/transaction-trends`     `ACCESS_ACCOUNTS_READ`
  GET      `/reports/transaction-trends/pdf` same
  GET      `/reports/account-mix`            `ACCESS_ACCOUNTS_READ`
  GET      `/reports/account-mix/pdf`        same
  GET      `/reports/approvals`              ADMIN | MANAGER | AUDITOR
  GET      `/reports/approvals/pdf`          same
  -----------------------------------------------------------------------

Query params typically include `from` / `to` (ISO dates) where
applicable. No hard 93-day cap (removed for learning flexibility).

## 7–13. Code map

  -----------------------------------------------------------------------
  Layer      Classes / paths
  ---------- ------------------------------------------------------------
  Backend    `com.bankone.report.*` — `ReportController`, services, DTOs,
             PDF helpers

  Frontend   `features/reports/reports.*` — Chart.js, category picker
  -----------------------------------------------------------------------

## 14–18. Validation / Security / Logging / Audit

- Approvals reports: role-gated in `SecurityConfig`.
- Large ranges may be slow on load-test DBs — candidate for caching /
  read-replica learning topics.

## 19. Testing Strategy

Compare trend totals to known deposit fixtures; open PDF in browser.

## 20. Future Extension Guide

Cursor aggregation jobs; cache daily buckets; see TECH_LEARNING_PLAN.

------------------------------------------------------------------------

# Future Modification Guide

### Requirement: Add branch filter to account mix

  -----------------------------------------------------------------------
  Item     Detail
  -------- --------------------------------------------------------------
  Files    Report DTO + service query; Angular filter control
  Impact   Needs consistent `branch_code` data quality
  -----------------------------------------------------------------------
