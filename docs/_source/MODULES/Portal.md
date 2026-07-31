# Portal (customer) & transfer approvals

## 1. Feature Overview

Customer self-service **portal** plus staff **transfer approvals**:

- Portal users (`CUSTOMER` role, `users.customer_id` set) see own accounts,
  transactions, beneficiaries, and can transfer.
- Transfers may execute immediately or create a **pending**
  `transfer_request` (threshold or OTHER_BANK).
- Staff with accounts write access resolve pending items; history on
  Approvals screen and Audit.

**Status:** Implemented (portal + beneficiaries + approvals). Other-bank
approve does **not** yet move external ledger (recorded approval only).

## 2. Business Purpose

Let customers bank online; escalate risky transfers to staff.

## 3. User Workflow

### Customer

1. Login → routed to `/portal/accounts`.
2. View account / transactions.
3. Manage beneficiaries; submit transfer (beneficiary or quick account #).
4. Outcome: `EXECUTED` or `PENDING_APPROVAL`.

### Staff

1. `/app/transfer-approvals` — pending queue + **my** resolution history.
2. Approve / reject (reject reason optional).
3. Full history also under Audit → Approval history.

## 4. Execution Flow

### Portal transfer

    PortalController.transfer
            ↓
    PortalTransferService.transfer
            ↓
    needsApproval? → TransferRequestEntity PENDING
                 : → AccountService.transfer (SAME_BANK)

### Staff approve (SAME_BANK)

    TransferApprovalService.approve
            ↓
    AccountService.transfer
            ↓
    status EXECUTED + audit TRANSFER_APPROVED

## 5. Database Tables

### `beneficiary`

Customer payees: SAME_BANK (linked account) or OTHER_BANK (IFSC/bank name).

### `transfer_request`

Pending/resolved portal transfers: amount, bank type, destination
snapshot, `requested_by`, `resolved_by`, reasons, timestamps.

### `customers.transfer_approval_threshold`

Optional amount gate for same-bank portal transfers.

### `users.customer_id`

Links portal login to customer.

## 6. REST APIs

### Portal --- `/portal` (`ACCESS_PORTAL_ACCOUNTS`)

  -----------------------------------------------------------------------
  Method   Path                                      Notes
  -------- ----------------------------------------- --------------------
  GET      `/portal/accounts`                        Own accounts
  GET      `/portal/accounts/{id}`                   Own account
  GET      `/portal/accounts/{id}/transactions`      Paged ledger
  POST     `/portal/accounts/{id}/transfer`          Body:
                                                     beneficiaryId **or**
                                                     toAccountNumber +
                                                     amount
  GET      `/portal/beneficiaries`                   Active list
  POST     `/portal/beneficiaries`                   Create
  DELETE   `/portal/beneficiaries/{id}`              Soft deactivate
  -----------------------------------------------------------------------

### Transfer approvals --- `/transfer-approvals` (`ACCESS_ACCOUNTS_WRITE`)

  -----------------------------------------------------------------------
  Method   Path                         Notes
  -------- ---------------------------- ---------------------------------
  GET      `/transfer-approvals`        Pending
  GET      `/transfer-approvals/my-     Current user's resolutions
           history`
  POST     `/transfer-approvals/{id}/   Approve
           approve`
  POST     `/transfer-approvals/{id}/   Reject + optional reason
           reject`
  -----------------------------------------------------------------------

## 7–13. Code map

`PortalController`, `PortalTransferService`, `PortalAccountService`,
`PortalCustomerContext`, `BeneficiaryService`, `TransferApprovalService`,
entities/repos under `beneficiary` / `transfer`.

Frontend: `features/portal/*`, `features` approvals screen,
`core/services/portal.ts`.

## 14. Validation Rules

- Portal can only touch own accounts.
- OTHER_BANK always needs approval.
- Same-bank needs approval when amount ≥ customer threshold (if set).
- Cannot add own account as beneficiary; quick transfer to own account
  blocked (own-account move still planned).

## 15. Security Rules

Portal authority separate from staff; JWT roles drive Angular
`portalGuard` vs staff layout.

## 16–18. Exception / Logging / Audit

Portal transfer request / execute and beneficiary changes write
`audit_event` (PORTAL / TRANSFER categories).

## 19. Testing Strategy

- Low-amount same-bank → EXECUTED
- High-amount / other-bank → PENDING → staff approve
- Reject → REJECTED, no ledger move

## 20. Future Extension Guide

Own-account internal move; external settlement saga; idempotency keys —
see TECH_LEARNING_PLAN.

------------------------------------------------------------------------

# Future Modification Guide

### Requirement: Portal own-account move

  -----------------------------------------------------------------------
  Item     Detail
  -------- --------------------------------------------------------------
  Files    `PortalTransferService` (lift own-account block); UI transfer
           mode
  Impact   Still uses `AccountService.transfer`; audit PORTAL_TRANSFER
  -----------------------------------------------------------------------
