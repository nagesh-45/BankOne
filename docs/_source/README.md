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
  -------------------------------------------------------------------------------------------

### Modules

  --------------------------------------------------------------------------------------------------------
  Module                 Status                 Doc
  ---------------------- ---------------------- ----------------------------------------------------------
  Authentication         Implemented            [MODULES/Authentication.md](./MODULES/Authentication.md)

  Customer               Implemented            [MODULES/Customer.md](./MODULES/Customer.md)

  Account                Implemented            [MODULES/Account.md](./MODULES/Account.md)

  Employee               Implemented            [MODULES/Employee.md](./MODULES/Employee.md)

  Transaction            Stub                   [MODULES/Transaction.md](./MODULES/Transaction.md)

  Branch                 Not implemented        [MODULES/Branch.md](./MODULES/Branch.md)
                         (branch code string    
                         only)                  

  Loan                   Partial                [MODULES/Loan.md](./MODULES/Loan.md)
                         (`AccountType.LOAN`    
                         only)                  

  Reports                Stub                   [MODULES/Reports.md](./MODULES/Reports.md)

  Audit                  Partial infra only     [MODULES/Audit.md](./MODULES/Audit.md)
  --------------------------------------------------------------------------------------------------------

## Documentation ownership rule

Whenever code changes, update the affected docs **in the same task**,
then append `CHANGELOG.md`. See
`.cursor/rules/documentation-architect.mdc`.
