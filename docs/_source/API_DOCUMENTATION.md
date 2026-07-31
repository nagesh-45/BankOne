# API Documentation

Base URL (Liberty): `http://localhost:9080`\
Auth header: `Authorization: Bearer <jwt>`

Unless noted, all endpoints require authentication.

**Side effect (implemented):** successful open account / deposit /
withdraw / transfer publishes a Kafka `BankActionEvent` and emails the
**customer** (`customers.email`). Not a separate REST resource — see
`MODULES/Notification.md`.

## Authentication --- `/auth`

  ------------------------------------------------------------------------------------------------
  Method     Path               Roles           Body / params              Response
  ---------- ------------------ --------------- -------------------------- -----------------------
  POST       `/auth/login`      **Public**      `{ username, password }`   `{ token, ... }`
                                                                           (`LoginResponse`)

  POST       `/auth/logout`     Authenticated   ---                        204 (writes AUTH
                                                                           LOGOUT audit)

  GET        `/auth/me`         Authenticated   ---                        `UserProfileResponse`

  PUT        `/auth/password`   Authenticated   `ChangePasswordRequest`    204
  ------------------------------------------------------------------------------------------------

## Dashboard --- `/dashboard`

  ---------------------------------------------------------------
  Method          Path            Roles           Response
  --------------- --------------- --------------- ---------------
  GET             `/dashboard`    Authenticated   Dashboard
                                                  summary DTO

  ---------------------------------------------------------------

## Customers --- `/customers`

  -----------------------------------------------------------------------------------------------------------------
  Method            Path                                           Roles           Notes
  ----------------- ---------------------------------------------- --------------- --------------------------------
  GET               `/customers?search&page&size&sortBy&sortDir`   ADMIN,          Page of `Customer`
                                                                   EMPLOYEE,       
                                                                   MANAGER         

  GET               `/customers/{id}`                              ADMIN,          
                                                                   EMPLOYEE,       
                                                                   MANAGER         

  POST              `/customers`                                   ADMIN, EMPLOYEE `CreateCustomerRequest`
                                                                                   (optional account fields)

  PUT               `/customers/{id}`                              ADMIN, EMPLOYEE `@Valid UpdateCustomerRequest`

  DELETE            `/customers/{id}`                              ADMIN           
  -----------------------------------------------------------------------------------------------------------------

### CreateCustomerRequest (key fields)

`firstName`, `lastName`, `email`, `phoneNumber`, `dateOfBirth`,
`address`, `status`, optional `branchCode`, `accountType`,
`currencyCode`, `openingDeposit`

## Accounts --- `/accounts`

  -----------------------------------------------------------------------------------------------------------------------------
  Method            Path                                                         Roles           Notes
  ----------------- ------------------------------------------------------------ --------------- ------------------------------
  POST              `/accounts`                                                  ADMIN, EMPLOYEE `OpenAccountRequest`

  GET               `/accounts?search&page&size&sortBy&sortDir`                  ADMIN,          Search page
                                                                                 EMPLOYEE,       
                                                                                 MANAGER         

  GET               `/accounts/customer/{customerId}?page&size&sortBy&sortDir`   ADMIN,          
                                                                                 EMPLOYEE,       
                                                                                 MANAGER         

  GET               `/accounts/{accountId}`                                      ADMIN,          Single `AccountResponse`
                                                                                 EMPLOYEE,       
                                                                                 MANAGER         

  GET               `/accounts/{accountId}/transactions?page&size&sortBy&sortDir` ADMIN,        Page of `TransactionResponse`
                                                                                 EMPLOYEE,       
                                                                                 MANAGER         

  PUT               `/accounts/{accountId}/status`                               ADMIN, EMPLOYEE `UpdateAccountStatusRequest`

  POST              `/accounts/{accountId}/deposit`                              ADMIN, EMPLOYEE `DepositRequest` `{ amount }`

  POST              `/accounts/{accountId}/withdraw`                             ADMIN, EMPLOYEE `WithdrawRequest` `{ amount }`

  POST              `/accounts/{accountId}/transfer`                             ADMIN, EMPLOYEE `TransferRequest` `{ toAccountId, amount }`
  -----------------------------------------------------------------------------------------------------------------------------

### OpenAccountRequest

`customerId`, `branchCode`, `accountType`, `currencyCode`,
`openingDeposit`, `createdBy`

### AccountResponse

Includes `accountId`, `accountNumber`, balances, `status`, `customerId`,
timestamps, etc.

### TransactionResponse (list under account)

`transactionId`, `accountId`, `transactionType`, `amount`,
`balanceAfter`, `currencyCode`, `narration`, `createdAt`, `createdBy`.

Sort whitelist for transactions: `createdAt`, `amount`, `transactionId`,
`transactionType` (default `createdAt` desc). 404-style business error
if account id does not exist.

## Account policies --- `/account-policies`

  -----------------------------------------------------------------------------------------------------------------
  Method            Path                                             Access                        Notes
  ----------------- ------------------------------------------------ ----------------------------- ------------------------------
  GET               `/account-policies?accountType=&currencyCode=`   POLICIES_MANAGE or            Active policy; default
                                                                     ACCOUNTS_READ/WRITE           currency `INR`

  GET               `/account-policies/all`                          same                          All policies (mgmt UI)

  PUT               `/account-policies/{id}`                         POLICIES_MANAGE               Update policy

  POST              `/account-policies`                              POLICIES_MANAGE               Create (if exposed)
  -----------------------------------------------------------------------------------------------------------------

### AccountPolicyResponse

`policyId`, `accountType`, `currencyCode`, `openingDepositRequired`,
`requiredOpeningDeposit`, `minimumBalance`, `active`, `effectiveFrom`,
`effectiveTo`

## Users (employees) --- `/users`

Requires `ACCESS_USERS_MANAGE` (typically ADMIN).

  ---------------------------------------------------------------
  Method          Path            Notes
  --------------- --------------- -------------------------------
  GET             `/users`        Paged employees + search

  POST            `/users`        Staff or portal user
                                  (`userType`, `roleNames[]`)

  PUT             `/users/{id}`   Update profile / roles /
                                  enabled
  ---------------------------------------------------------------

## Roles --- `/roles`

`ACCESS_ROLES_MANAGE` or `ACCESS_USERS_MANAGE`.

  ---------------------------------------------------------------
  Method          Path            Notes
  --------------- --------------- -------------------------------
  GET             `/roles`        List roles + access codes

  GET             `/roles/{id}`   Detail

  POST            `/roles`        Create

  PUT             `/roles/{id}`   Update description / accesses
  ---------------------------------------------------------------

## Transactions (staff) --- `/transactions`

`ACCESS_ACCOUNTS_READ`. Paged search across ledger.

  GET `/transactions?accountId&txType&search&page&size&sortBy&sortDir`

## Portal --- `/portal`

`ACCESS_PORTAL_ACCOUNTS`. See [MODULES/Portal.md](./MODULES/Portal.md).

## Transfer approvals --- `/transfer-approvals`

`ACCESS_ACCOUNTS_WRITE`. Pending / my-history / approve / reject.
See Portal module doc.

## Audit --- `/audit`

ADMIN | MANAGER | AUDITOR. See [MODULES/Audit.md](./MODULES/Audit.md).

  GET `/audit/events?category&action&actor&page&size`  
  GET `/audit/transfer-approvals`  
  POST `/audit/backfill` (ADMIN)

## Reports --- `/reports`

See [MODULES/Reports.md](./MODULES/Reports.md).

  GET `/reports/transaction-trends` (+ `/pdf`)  
  GET `/reports/account-mix` (+ `/pdf`)  
  GET `/reports/approvals` (+ `/pdf`) — Admin/Manager/Auditor

## Misc

  --------------------------------------------------------------
  Method               Path                 Roles
  -------------------- -------------------- --------------------
  GET                  `/api/hello`         Authenticated

  --------------------------------------------------------------

## Error shape

Handled by `GlobalExceptionHandler` under `com.bankone.common` (business
exceptions → 4xx with message body). Exact payload may vary by exception
type. Rate-limit learning work will add HTTP 429 later
([TECH_LEARNING_PLAN.md](./TECH_LEARNING_PLAN.md)).

## Frontend API clients

  ---------------------------------------------------------------------------------
  Service                        File
  ------------------------------ --------------------------------------------------
  Auth                           `BankOne-Frontend/src/app/core/services/auth.ts`

  Customer                       `.../customer.ts`

  Account                        `.../account.ts`

  Account policy                 `.../account-policy.ts`

  User                           `.../user.ts`

  Dashboard                      `.../dashboard.service.ts`

  Portal / approvals / audit     `.../portal.ts`

  Reports                        `features/reports` (+ HTTP in component/service)
  ---------------------------------------------------------------------------------
