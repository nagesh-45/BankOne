# Branch

## 1. Feature Overview

Branch master (codes, name, address, IFSC-like metadata). **Not
implemented** as a module. Accounts store free-text `branchCode` (often
hardcoded `0001`).

**Status:** Stub / Planned

## 2. Business Purpose

Multi-branch operations, reporting by branch, validated branch codes in
account numbers.

## 3. User Workflow

Planned: ADMIN maintains branches; account open selects branch from
list.

## 4. Execution Flow

Today:

    OpenAccountRequest.branchCode (string)
            ↓
    AccountNumberGenerator.generate(branchCode, ...)

## 5. Database Tables

None. Planned: `branch`

## 6. REST APIs

None. Planned: `/branches` CRUD

## 7--13. Controllers / Services / Repositories / DTOs / Entities / Utils / Config

None

## 14. Validation Rules

Today: none beyond non-null string on open. Planned: FK exists and
active.

## 15. Security Rules

Planned: ADMIN write; broader read

## 16--18. Exception / Logging / Audit

N/A

## 19. Testing Strategy

When built: reject unknown branchCode on open account

## 20. Future Extension Guide

Add `branch` entity; migrate existing `account.branch_code` values; wire
Open Account / Customer create dialogs.

------------------------------------------------------------------------

# Future Modification Guide

### Requirement: Add branch master and validate on open

  -------------------------------------------------------------
  Item                      Detail
  ------------------------- -----------------------------------
  Files                     New `com/bankone/branch/**`;
                            `AccountServiceImpl.openAccount`;
                            Angular account/customer dialogs

  Classes                   `BranchController`,
                            `BranchService`,
                            `AccountServiceImpl`

  Methods                   `openAccount()` ---
                            `branchRepository.findByCode`
                            before generate

  Impact                    Account number generation; reports;
                            employee home-branch later
  -------------------------------------------------------------

### Call hierarchy (target)

    AccountController.openAccount()
            ↓
    AccountServiceImpl.openAccount()
            ↓
    BranchService.requireActive(branchCode)
            ↓
    AccountNumberGenerator.generate()
            ↓
    AccountRepository.save()
