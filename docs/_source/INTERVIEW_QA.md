# BankOne Interview Questions and Answers

This note is based on the current BankOne backend and frontend code.

## Architecture and stack

**1. What is BankOne?**  
BankOne is a core banking platform with Spring Boot backend services, an Angular frontend, PostgreSQL persistence, JWT security, and Kafka-based notification handling.

**2. What are the main application layers?**  
Controller, service, repository, entity, DTO, security, and frontend UI layers. Controllers expose REST APIs, services contain business logic, repositories handle persistence.

**3. Why is Open Liberty used?**  
The backend is packaged as a WAR and runs on Open Liberty for local deployment and Java EE-style hosting. The same app can also run with embedded Boot for Docker/cloud.

**4. What database pattern is used?**  
Spring Data JPA with PostgreSQL. Entities map to tables, and repositories provide query methods and pagination.

**5. Why are DTOs used?**  
To keep API payloads separate from persistence entities and avoid leaking database structure to the frontend.

**6. Why do repositories use interfaces only?**  
Spring Data generates implementations automatically from method names and specifications.

**7. What is the role of `AuditableEntity`?**  
It provides shared `createdAt`, `updatedAt`, `createdBy`, `updatedBy`, and `version` fields for all auditable tables.

**8. Why is optimistic locking useful here?**  
`@Version` helps prevent accidental overwrites when concurrent updates happen on the same record.

## Authentication and authorization

**9. How is login handled?**  
`POST /auth/login` delegates to `AuthenticationService`, which validates credentials and returns a JWT-based `LoginResponse`.

**10. How does the app know the current user?**  
`GET /auth/me` returns the current authenticated user profile using the security context.

**11. How are passwords changed?**  
`PUT /auth/password` accepts a validated request and updates the password through `AuthenticationService`.

**12. How is role-based access enforced?**  
Backend methods use `@PreAuthorize`, and the Angular frontend uses route data plus `auth.hasAnyRole(...)` and route guards.

**13. What roles exist in the system?**  
The code seeds roles like ADMIN, MANAGER, EMPLOYEE, TELLER, AUDITOR, and CUSTOMER through `RoleInitializer`.

**14. Why use JWT instead of sessions?**  
JWT keeps the backend stateless, which fits API-based frontend/mobile access and simpler horizontal scaling.

**15. What does `SecurityConfig` do?**  
It defines the security filter chain, CORS, JWT filter placement, stateless sessions, and role-based URL access.

**16. Why is the JWT filter disabled as a servlet filter?**  
To avoid double registration. It must run only inside Spring Security’s filter chain.

**17. Why is `PasswordEncoder` BCrypt?**  
BCrypt is a secure adaptive hash for passwords and is the standard choice in Spring Security.

**18. What does `LoginAttemptService` imply?**  
Failed logins are counted so accounts can be locked after repeated bad attempts.

**19. What happens when an account is locked?**  
`AuthenticationService.login()` throws `LockedException`, and the global exception handler maps it to a response.

## Customer and employee management

**20. How are customers created?**  
`POST /customers` calls `CustomerService.createCustomer`, which saves the customer and then opens the first account.

**21. What happens if a customer email or phone already exists?**  
The service checks uniqueness and throws a conflict error before insertion.

**22. Why does customer creation also open an account?**  
This product assumes onboarding means customer creation plus initial banking access in one flow.

**23. How are employees created?**  
`POST /users` is admin-only. `UserService.createUser` creates the user, assigns a role, and returns a response with a business employee code.

**24. How are employee roles updated?**  
`PUT /users/{id}` updates the user record and switches the active role between ADMIN and EMPLOYEE access levels.

**25. How are employee lists searched?**  
`GET /users` supports search, paging, and sorting via `PageRequests.of(...)` and `EmployeeSpecification`.

**26. How is a business employee code generated?**  
The frontend and backend both use the same formatting convention through `BusinessIdFormatter.employeeCode(...)`.

**27. Why store both `User` and `UserRole`?**  
`User` stores identity and login data; `UserRole` stores role history and active/inactive role assignment.

**28. What is the purpose of `RoleInitializer`?**  
It seeds default roles at startup so the system can assign roles without manual DB setup.

**29. Why use `@PreAuthorize` on `UserController`?**  
Because user management is admin-only and should be blocked at the service endpoint.

**30. What does the frontend do for employees?**  
It provides create/edit/list dialogs and uses role claims to hide admin-only features.

## Accounts and policies

**31. How are accounts opened?**  
`AccountService.openAccount` creates the account using the active account policy, generates an account number, and records an opening transaction if needed.

**32. What is the account policy feature?**  
`/account-policies` lets the app create and fetch active policies by account type and currency.

**33. Why does the code check policy effective dates?**  
To prevent using policies that are not yet valid or already expired.

**34. Why does opening an account need `branchCode`?**  
Branch code is part of the account number generation and the business identity of the account.

**35. What does `AccountNumberGenerator` solve?**  
It builds a business-safe account number from branch, type, currency, and ordinal.

**36. Why keep `ordinal` in the account table?**  
It gives a stable sequence number for deterministic account numbering and business formatting.

**37. What is the meaning of `availableBalance` vs `ledgerBalance`?**  
`availableBalance` is spendable balance; `ledgerBalance` tracks posted ledger state.

**38. Why update both balances together?**  
The app keeps operational and ledger balances aligned for this banking model.

**39. Why is account status stored as a string?**  
The entity stores the enum name, making DB rows easy to read and simplifying status transitions.

**40. What does `updateAccountStatus` do?**  
It flips the status, sets closed/open timestamps when needed, and persists the change.

## Transactions and money movement

**41. How do deposit, withdraw, and transfer work?**  
They validate inputs, update balances, record transactions, and publish notification events.

**42. Why are transfers locked in ascending account-id order?**  
To reduce deadlock risk when two accounts are updated in one transaction.

**43. How are withdrawals protected from race conditions?**  
`findByIdForUpdate` locks the row before balance subtraction.

**44. Why does deposit not lock the row the same way withdrawal does?**  
Withdrawal is the more sensitive insufficient-funds path; deposit is simpler but still transactional.

**45. How are transactions stored?**  
Every money movement is persisted through `TransactionService.record(...)` into the transaction table.

**46. Why is transaction recording separated from account mutation?**  
So the balance update and ledger insert are explicit business steps and easier to reuse.

**47. Why is `TransactionType` an enum?**  
It keeps transaction categories controlled and avoids free-text values.

**48. Why is `TransactionService.getByAccountId` read-only?**  
It only reads ledger history and should not participate in write transactions.

**49. How are account and transaction lists paginated?**  
Controllers build `Pageable` objects using `PageRequests.of(...)` with allowed sort fields.

**50. Why are allowed sort fields restricted?**  
To prevent invalid or unsafe sort values from the client.

**51. Why does transfer write two transaction rows?**  
One debit row on the source account and one credit row on the destination account.

**52. What should happen if transfer currency differs?**  
The service rejects it with a currency mismatch error because this banking model only supports same-currency transfer.

## Notifications and Kafka

**53. Why is Kafka used?**  
Kafka is used for asynchronous side effects, especially notification emails, so banking operations stay fast and transactional.

**54. Is `app.kafka.notification-topic` a queue?**  
Functionally yes for this app, but technically it is a Kafka topic used as an event stream.

**55. What does the notification publisher do?**  
`NotificationEventPublisher` creates a `BankActionEvent` and sends it to Kafka.

**56. What does the notification consumer do?**  
`NotificationEventConsumer` listens to Kafka, composes an email, and sends it through the configured mail service.

**57. Why should Kafka not wrap core balance updates?**  
Balance updates must remain synchronous and consistent. Kafka should handle only side effects like notifications, audit events, or downstream sync jobs.

**58. Why is a consumer group used?**  
It allows listeners to scale and distribute work across instances while keeping one delivery path per partition assignment.

**59. What is the risk in the current publisher?**  
The publisher logs Kafka failures but does not retry or persist failed events, so notifications can be lost.

**60. What is the next reliability improvement?**  
Transactional outbox or retry/error-topic handling to avoid event loss.

## Frontend

**61. How is the frontend organized?**  
Angular standalone components are grouped by feature: auth, customers, employees, accounts, dashboard, profile, and management.

**62. How does the frontend enforce roles?**  
The `Auth` service stores roles from login and helper methods like `hasAnyRole(...)` control route access and UI visibility.

**63. Where is the API base URL configured?**  
In `src/app/core/config/api-config.ts`, which reads from the environment files.

**64. How are API failures shown to the user?**  
Dialogs and pages use a shared `apiErrorMessage(...)` utility plus notification toasts.

**65. What frontend pattern is used for lists?**  
Signals + RxJS streams (`toSignal`, `combineLatest`, `switchMap`) drive search, paging, sorting, and loading state.

**66. Why do customer and account lists use signals?**  
To keep UI state reactive without manual subscription cleanup.

**67. How do list pagination components work?**  
They translate page index/size into API paging parameters and expose controls like first/last/jump.

**68. Why is the management page important?**  
It’s the admin hub for creating customers and staff and for future admin utilities.

**69. How are dialogs used in the UI?**  
Create/edit actions open Material dialogs, and the closing result triggers refresh/navigation.

**70. Why does the UI show business codes like C00001 / E00001?**  
They’re friendlier than raw numeric ids and match business-facing workflows.

## Deployment and runtime

**71. Where are logs written on Open Liberty?**  
The main runtime log is `messages.log`; application file logging can be configured separately through Logback.

**72. How do you debug SQL activity?**  
Hibernate SQL and bind logging are enabled through `application.properties` log levels.

**73. Why does the app support both Liberty and embedded Boot?**  
It gives local Java app-server deployment plus cloud/container flexibility.

**74. What is the role of CORS config?**  
To allow the Angular frontend host to call backend APIs without browser blocking.

**75. Why is the app stateless?**  
It uses JWT and no server-side session storage, which simplifies scaling.

## Design tradeoffs and future work

**76. Why was customer creation coupled with account opening?**  
It matches the current onboarding flow, but it could be split later if onboarding becomes multi-step.

**77. Why is role data currently used only for access control?**  
Because the product has not yet added a UI/API to manage roles as a first-class admin feature.

**78. What would be a good next queue use case?**  
Audit/event publishing for customer creation, role changes, and money-movement notifications.

**79. What would be a good next persistence improvement?**  
A proper outbox table for Kafka events and a retry worker.

**80. What would be a good next frontend improvement?**  
A real management roles page and listener/event monitoring page.

## Good interview follow-up questions

- Why did you choose JWT instead of server sessions?
- Why did you keep balance updates synchronous but notifications async?
- How would you add a new role-management screen safely?
- How would you make notification delivery reliable across Kafka failures?
- How would you add audit history for money movement?
- How would you introduce a transactional outbox here?
- How would you support branch-level permissions later?
- How would you prevent duplicate onboarding requests?
