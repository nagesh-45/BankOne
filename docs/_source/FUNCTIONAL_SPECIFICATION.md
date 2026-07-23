# Functional Specification

Status legend: **Implemented** · **Partial** · **Stub / Planned**

## 1. Authentication & session

  ------------------------------------------------------------------
  Capability            Status               Notes
  --------------------- -------------------- -----------------------
  Staff login           Implemented          JWT returned
  (username/password)                        

  Current user profile  Implemented          
  (`/auth/me`)                               

  Change password       Implemented          Min 8 chars; confirm
                                             match

  Remember-me (local vs Implemented          Frontend
  session storage)                           

  Failed-attempt        Implemented          `LoginAttemptService`
  lockout                                    

  Role-based route      Implemented          Angular `authGuard`
  guards                                     
  ------------------------------------------------------------------

## 2. Dashboard

  ------------------------------------------------------------
  Capability                 Status            Notes
  -------------------------- ----------------- ---------------
  Summary counts (customers, Implemented       
  accounts, employees)                         

  Today's transaction count  Partial           Hardcoded `0`
                                               until
                                               Transaction
                                               module exists
  ------------------------------------------------------------

## 3. Customers

  ------------------------------------------------------------
  Capability                 Status            Notes
  -------------------------- ----------------- ---------------
  Search / paginate          Implemented       
  customers                                    

  View customer detail       Implemented       

  Create customer            Implemented       Optional first
                                               account on
                                               create

  Update customer            Implemented       

  Soft/hard delete           Implemented       ADMIN only for
                                               DELETE

  Open additional accounts   Implemented       Current
  from detail                                  (policy-aware
                                               opening
                                               deposit); Loan
                                               button present
  ------------------------------------------------------------

## 4. Accounts

  ----------------------------------------------------------------------
  Capability                 Status            Notes
  -------------------------- ----------------- -------------------------
  Open account               Implemented       Policy-validated opening
                                               deposit

  List / search accounts     Implemented       `/app/accounts`

  List accounts by customer  Implemented       

  Update account status      Implemented       API present

  Deposit (post-open)        Implemented       Credits balances only; no
                                               ledger row

  Withdraw / transfer        Stub              Not built

  Account detail + ledger UI Stub              Not built

  Account policies (create + Implemented       Seeded INR policies; no
  get active)                                  LOAN seed

  Opening-deposit dialog     Implemented       Uses
  (Current)                                    `GET /account-policies`
  ----------------------------------------------------------------------

## 5. Employees (users)

  ------------------------------------------------------------
  Capability                 Status            Notes
  -------------------------- ----------------- ---------------
  List / create / update     Implemented       ADMIN
  employees                                    

  Role assignment            Implemented       Seeded roles
                                               include unused
                                               TELLER /
                                               AUDITOR /
                                               CUSTOMER
  ------------------------------------------------------------

## 6. Branch

  --------------------------------------------------------------
  Capability           Status               Notes
  -------------------- -------------------- --------------------
  Branch master data   Stub                 Only `branchCode`
                                            string (often
                                            `0001`)

  --------------------------------------------------------------

## 7. Loans

  ------------------------------------------------------------
  Capability                 Status            Notes
  -------------------------- ----------------- ---------------
  Loan product / EMI /       Stub              
  collateral                                   

  `AccountType.LOAN` account Partial           Enum + UI
  opening                                      affordance;
                                               needs policy;
                                               not a loan
                                               domain
  ------------------------------------------------------------

## 8. Transactions & reports

  --------------------------------------------------------------
  Capability           Status               Notes
  -------------------- -------------------- --------------------
  Transaction ledger   Stub                 Empty `transaction`
  entity/API                                package

  Reports              Stub                 Empty `report`
                                            package; sidebar
                                            placeholder

  Beneficiaries        Stub                 Empty `beneficiary`
                                            package
  --------------------------------------------------------------

## 9. Audit

  ----------------------------------------------------------------
  Capability                 Status            Notes
  -------------------------- ----------------- -------------------
  JPA created/updated by     Partial           `AuditableEntity`
  fields                                       on some entities

  Audit event API / UI       Stub              Empty `audit`
                                               package
  ----------------------------------------------------------------

## Non-functional (current)

  -------------------------------------------------------------
  Area                           Behavior
  ------------------------------ ------------------------------
  AuthN                          JWT HMAC, 1h expiry
                                 (`jwt.expiration`)

  AuthZ                          Role matchers in
                                 `SecurityConfig`

  CORS                           localhost, bankone.local,
                                 192.168.0.4

  Logging                        Hibernate SQL DEBUG
                                 (dev-oriented)

  Deploy                         Open Liberty WAR primary path
  -------------------------------------------------------------

Deferred hardening items are tracked in
`.cursor/rules/deferred-hardening.mdc`.
