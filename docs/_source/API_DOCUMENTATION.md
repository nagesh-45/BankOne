# API Documentation

Base URL (Liberty): `http://localhost:9080`\
Auth header: `Authorization: Bearer <jwt>`

Unless noted, all endpoints require authentication.

## Authentication --- `/auth`

  ------------------------------------------------------------------------------------------------
  Method     Path               Roles           Body / params              Response
  ---------- ------------------ --------------- -------------------------- -----------------------
  POST       `/auth/login`      **Public**      `{ username, password }`   `{ token, ... }`
                                                                           (`LoginResponse`)

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
  Method            Path                                             Roles           Notes
  ----------------- ------------------------------------------------ --------------- ------------------------------
  POST              `/account-policies`                              Authenticated   `CreateAccountPolicyRequest`
                                                                     only\*          

  GET               `/account-policies?accountType=&currencyCode=`   Authenticated   Active policy; default
                                                                     only\*          currency `INR`
  -----------------------------------------------------------------------------------------------------------------

\*Deferred hardening: restrict create (and possibly GET) to ADMIN.

### AccountPolicyResponse

`policyId`, `accountType`, `currencyCode`, `openingDepositRequired`,
`requiredOpeningDeposit`, `minimumBalance`, `active`, `effectiveFrom`,
`effectiveTo`

## Users (employees) --- `/users`

  ---------------------------------------------------------------
  Method          Path            Roles           Notes
  --------------- --------------- --------------- ---------------
  GET             `/users`        ADMIN           

  POST            `/users`        ADMIN           

  PUT             `/users/{id}`   ADMIN           
  ---------------------------------------------------------------

## Misc

  --------------------------------------------------------------
  Method               Path                 Roles
  -------------------- -------------------- --------------------
  GET                  `/api/hello`         Authenticated

  --------------------------------------------------------------

## Error shape

Handled by `GlobalExceptionHandler` under `com.bankone.common` (business
exceptions → 4xx with message body). Exact payload may vary by exception
type.

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
  ---------------------------------------------------------------------------------
