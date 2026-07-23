# Loan

## 1. Feature Overview

Full loan product (sanction, disbursement, EMI, interest, collateral) is
**not** implemented. Only `AccountType.LOAN` exists on the Account
module, plus a UI "Add Loan Account" affordance. No LOAN row in
`AccountPolicyInitializer` seed.

**Status:** Partial (account type only)

## 2. Business Purpose

Eventually manage lending products separately from deposit accounts
while optionally linking a LOAN account for disbursement/repayment
postings.

## 3. User Workflow

Today: Customer detail → Add Loan Account may attempt open; likely fails
policy lookup until policy exists.\
Planned: Loan application → approval → disbursement → schedule.

## 4. Execution Flow

Today reuses Account open flow with `AccountType.LOAN`.

## 5. Database Tables

None dedicated. Uses `account` if a LOAN account is opened.

## 6. REST APIs

None loan-specific; uses `POST /accounts`

## 7--13. Controllers / Services / ...

None under a loan package. Account classes apply.

## 14. Validation Rules

Account policy required for open --- **no LOAN seed** → open fails until
policy created via API.

## 15. Security Rules

Same as accounts when using account APIs

## 16--18. Exception / Logging / Audit

Same as Account module

## 19. Testing Strategy

- Document failure without policy
- After `POST /account-policies` for LOAN/INR, open succeeds as generic
  account only

## 20. Future Extension Guide

Do not overload `account` forever --- introduce `loan_application`,
`loan_account` link, schedule tables; disbursement posts via Transaction
module.

------------------------------------------------------------------------

# Future Modification Guide

### Requirement: Allow opening LOAN accounts like CURRENT

  ------------------------------------------------------------
  Item                      Detail
  ------------------------- ----------------------------------
  Files                     `AccountPolicyInitializer.java` or
                            POST policy; `customer-detail.ts`
                            (opening deposit dialog)

  Classes                   `AccountPolicyInitializer`,
                            `AccountPolicyServiceImpl`, UI
                            detail component

  Methods                   Seed/`createPolicy`; reuse
                            `getActivePolicy('LOAN')` + dialog
                            pattern from Current

  Impact                    Still **not** a loan product ---
                            balances only
  ------------------------------------------------------------

### Requirement: Build real loan domain

  ------------------------------------------------------------
  Item                      Detail
  ------------------------- ----------------------------------
  Files                     New `com/bankone/loan/**`;
                            SecurityConfig; Angular features

  Impact                    Transaction
                            disbursement/repayment; Reports;
                            Audit; Account may hold
                            disbursement account only
  ------------------------------------------------------------

### Call hierarchy (today)

    UI addLoanAccount()
            ↓
    AccountController.openAccount()
            ↓
    AccountServiceImpl.openAccount()
            ↓
    AccountPolicyRepository (may miss LOAN)
