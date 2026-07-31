# BankOne Documentation

Living documentation for the BankOne core banking platform. **Docs are
part of the source code** and must stay synchronized with every feature
change.

## Repository layout

  -------------------------------------------------------------
  Path                           Description
  ------------------------------ ------------------------------
  `BankOne-BackEnd/`              Spring Boot 4 / Java 21
                                 backend (WAR, Open Liberty)

  `BankOne-Frontend/`            Angular frontend (`ng serve` →
                                 port 4200)

  `docs/`                        This documentation set
  -------------------------------------------------------------

## Quick start

  ------------------------------------------------------------------------------------
  Component          How to run                              Default URL
  ------------------ --------------------------------------- -------------------------
  Backend (Liberty)  `BankOne/scripts/redeploy-liberty.sh`   `http://localhost:9080`
                     (JDWP debug attach `:7777`)

  Kafka + Mailpit    `docker compose up -d kafka mailpit`    Kafka `:9092` /
                                                             Mailpit UI `:8025`

  Load-test seed     `psql … -f scripts/seed-loadtest-10k.sql` 10k LoadTest rows

  Backend (embedded  `mvn spring-boot:run`                   `http://localhost:8080`
  Boot)                                                      

  Frontend           `cd BankOne-Frontend && ng serve`       `http://localhost:4200`
  ------------------------------------------------------------------------------------

Default staff login: `admin` / `Admin@123`

API base used by the UI:
`BankOne-Frontend/src/app/core/config/api-config.ts`

## Documentation map

  -------------------------------------------------------------------------------------------
  Document                                                       Purpose
  -------------------------------------------------------------- ----------------------------
  [ARCHITECTURE.md](./ARCHITECTURE.md)                           System layers, packages,
                                                                 deployment

  [TECH_STACK.md](./TECH_STACK.md)                               Technologies & versions
                                                                 (keep current)

  [FUNCTIONAL_SPECIFICATION.md](./FUNCTIONAL_SPECIFICATION.md)   What the product does
                                                                 (implemented vs planned)

  [DEVELOPMENT_GUIDE.md](./DEVELOPMENT_GUIDE.md)                 Local setup, conventions,
                                                                 teach-while-building

  [API_DOCUMENTATION.md](./API_DOCUMENTATION.md)                 REST endpoints, roles,
                                                                 payloads

  [DATABASE_SCHEMA.md](./DATABASE_SCHEMA.md)                     Tables, sequences, ER notes

  [CALL_FLOW.md](./CALL_FLOW.md)                                 Call hierarchies for major
                                                                 flows

  [CLASS_DIAGRAM.md](./CLASS_DIAGRAM.md)                         Mermaid class / package
                                                                 diagrams

  [SEQUENCE_DIAGRAMS.md](./SEQUENCE_DIAGRAMS.md)                 Mermaid sequence diagrams

  [CHANGE_IMPACT_GUIDE.md](./CHANGE_IMPACT_GUIDE.md)             What breaks when you change
                                                                 X

  [EXTENSION_GUIDE.md](./EXTENSION_GUIDE.md)                     How to add modules safely

  [CHANGELOG.md](./CHANGELOG.md)                                 Append-only change history

  [INTERVIEW_QA.md](./INTERVIEW_QA.md)                           Interview questions and
                                                                  answers from codebase

  [TECH_LEARNING_PLAN.md](./TECH_LEARNING_PLAN.md)               Platform engineering learning
                                                                  roadmap (rate limit, cache,
                                                                  idempotency, sharding lab, …)
  -------------------------------------------------------------------------------------------

### Modules

  --------------------------------------------------------------------------------------------------------
  Module                 Status                 Doc
  ---------------------- ---------------------- ----------------------------------------------------------
  Authentication         Implemented            [MODULES/Authentication.md](./MODULES/Authentication.md)

  Customer               Implemented            [MODULES/Customer.md](./MODULES/Customer.md)

  Account                Implemented            [MODULES/Account.md](./MODULES/Account.md)

  Notification           Implemented            [MODULES/Notification.md](./MODULES/Notification.md)
                                                 (Kafka → email)

  Employee               Implemented            [MODULES/Employee.md](./MODULES/Employee.md)
                                                 (multi-role staff)

  Transaction            Implemented            [MODULES/Transaction.md](./MODULES/Transaction.md)
                                                 (ledger + staff list UI)

  Portal & approvals     Implemented            [MODULES/Portal.md](./MODULES/Portal.md)
                                                 (beneficiaries, pending
                                                 transfers)

  Reports                Implemented            [MODULES/Reports.md](./MODULES/Reports.md)
                                                 (charts + PDF)

  Audit                  Implemented            [MODULES/Audit.md](./MODULES/Audit.md)
                                                 (event trail + backfill)

  Branch                 Not implemented        [MODULES/Branch.md](./MODULES/Branch.md)
                         (branch code string    
                         only)                  

  Loan                   Partial                [MODULES/Loan.md](./MODULES/Loan.md)
                         (`AccountType.LOAN`    
                         only)                  
  --------------------------------------------------------------------------------------------------------

## Documentation ownership rule

Whenever code changes, update the affected docs **in the same task**,
then append `CHANGELOG.md`. See
`.cursor/rules/documentation-architect.mdc`.
