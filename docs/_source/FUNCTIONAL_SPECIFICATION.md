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

  Logout (API + audit)  Implemented          `POST /auth/logout`
                                             records AUTH LOGOUT

  Remember-me (local vs Implemented          Frontend
  session storage)                           

  Failed-attempt        Implemented          `LoginAttemptService`
  lockout                                    

  Role-based route      Implemented          Angular `authGuard` /
  guards                                     `portalGuard`
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

  Get account by id          Implemented       `GET /accounts/{id}`

  Update account status      Implemented       API present

  Deposit (post-open)        Implemented       Credits balances + writes
                                               CREDIT ledger row; Kafka
                                               email to customer

  Withdraw                   Implemented       DEBIT + Kafka email

  Transfer                   Implemented       DEBIT+CREDIT; Kafka email
                                               to destination customer

  Account detail + ledger UI Implemented       Account detail + paged
                                               transactions

  List pagination            Implemented       First/Last + Go to page
                                               (`list-pagination`)

  Account policies (create + Implemented       Seeded INR policies; no
  get active)                                  LOAN seed

  Opening-deposit dialog     Implemented       Uses
  (Current)                                    `GET /account-policies`

  Kafka customer email       Implemented       See MODULES/Notification.md
  ----------------------------------------------------------------------

## 5. Employees (users)

  ------------------------------------------------------------
  Capability                 Status            Notes
  -------------------------- ----------------- ---------------
  List / create / update     Implemented       ADMIN; multi-role
  employees                                    assignment
                                               (`roleNames`)

  Role CRUD + access codes   Implemented       `/roles`; union of
                                               role authorities at
                                               login

  Account policies UI        Implemented       Management card;
                                               ADMIN/MANAGER edit
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
  Transaction ledger   Implemented          Write on deposit /
  entity/API                                withdraw / transfer /
                                            opening; staff
                                            `/transactions` UI;
                                            account-detail UI;
                                            dashboard today-count
                                            still stub

  Reports (charts +    Implemented          Trends, account mix,
  PDF)                                      approvals; OpenPDF

  Beneficiaries        Implemented          Portal CRUD
  --------------------------------------------------------------

## 9. Customer portal & approvals

  ----------------------------------------------------------------
  Capability                 Status            Notes
  -------------------------- ----------------- -------------------
  Portal accounts / txn      Implemented       `/portal/*`
  list                                         

  Portal transfer            Implemented       Immediate or
                                               pending approval

  Transfer approval          Implemented       Staff queue + my
  workflow                                     history

  Transfer approval          Implemented       
  threshold on customer                        
  ----------------------------------------------------------------

## 10. Notifications

  ----------------------------------------------------------------
  Capability                 Status            Notes
  -------------------------- ----------------- -------------------
  Kafka bank-action events   Implemented       Topic
                                               `bankone.notifications`

  Customer email on open /   Implemented       Recipient =
  deposit / withdraw /                         `customers.email`
  transfer                                     

  Local Mailpit / Render     Implemented       See
  SendGrid HTTPS                               MODULES/Notification.md
                                               (`APP_KAFKA_ENABLED`)
  ----------------------------------------------------------------

## 11. Audit

  ----------------------------------------------------------------
  Capability                 Status            Notes
  -------------------------- ----------------- -------------------
  JPA created/updated by     Partial           `AuditableEntity`
  fields                                       on some entities

  Activity event trail API   Implemented       Categories incl.
  / UI                                         AUTH login/logout

  Transfer approval audit    Implemented       
  history                                      

  Historical backfill        Implemented       Admin; idempotent
  ----------------------------------------------------------------

## Non-functional (current)

  -------------------------------------------------------------
  Area                           Behavior
  ------------------------------ ------------------------------
  AuthN                          JWT HMAC, 1h expiry
                                 (`jwt.expiration`)

  AuthZ                          Role + `ACCESS_*` authorities
                                 (`AppAccess` / `SecurityConfig`)

  CORS                           localhost, bankone.local,
                                 192.168.0.4

  Logging                        Hibernate SQL DEBUG
                                 (dev-oriented)

  Deploy                         Open Liberty WAR primary path;
                                 Render Docker + Aiven + SendGrid

  Messaging                      Kafka (optional via
                                 `APP_KAFKA_ENABLED`)

  Learning roadmap               See TECH_LEARNING_PLAN.md
                                 (rate limit, cache, outbox, …)
  -------------------------------------------------------------

Deferred hardening items are tracked in
`.cursor/rules/deferred-hardening.mdc`.
Platform learning topics (not the same as hardening) are tracked in
[TECH_LEARNING_PLAN.md](./TECH_LEARNING_PLAN.md).
