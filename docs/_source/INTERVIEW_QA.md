# BankOne Interview Questions and Answers

This note is based on the current BankOne backend and frontend code.

## Architecture

**1. What is BankOne?**  
BankOne is a core banking platform with Spring Boot backend services, an Angular frontend, PostgreSQL persistence, JWT security, and Kafka-based notification handling.

**2. What are the main application layers?**  
Controller, service, repository, entity, DTO, security, and frontend UI layers. Controllers expose REST APIs, services contain business logic, repositories handle persistence.

**3. Why is Open Liberty used?**  
The backend is packaged as a WAR and runs on Open Liberty for local deployment and Java EE-style hosting. The same app can also run with embedded Boot for Docker/cloud.

**4. What database pattern is used?**  
Spring Data JPA with PostgreSQL. Entities map to tables, and repositories provide query methods and pagination.

## Authentication and authorization

**5. How is login handled?**  
`POST /auth/login` delegates to `AuthenticationService`, which validates credentials and returns a JWT-based `LoginResponse`.

**6. How does the app know the current user?**  
`GET /auth/me` returns the current authenticated user profile using the security context.

**7. How are passwords changed?**  
`PUT /auth/password` accepts a validated request and updates the password through `AuthenticationService`.

**8. How is role-based access enforced?**  
Backend methods use `@PreAuthorize`, and the Angular frontend uses route data plus `auth.hasAnyRole(...)` and route guards.

**9. What roles exist in the system?**  
The code seeds roles like ADMIN, MANAGER, EMPLOYEE, TELLER, AUDITOR, and CUSTOMER through `RoleInitializer`.

## Customer and employee management

**10. How are customers created?**  
`POST /customers` calls `CustomerService.createCustomer`, which saves the customer and then opens the first account.

**11. What happens if a customer email or phone already exists?**  
The service checks uniqueness and throws a conflict error before insertion.

**12. How are employees created?**  
`POST /users` is admin-only. `UserService.createUser` creates the user, assigns a role, and returns a response with a business employee code.

**13. How are employee roles updated?**  
`PUT /users/{id}` updates the user record and switches the active role between ADMIN and EMPLOYEE access levels.

**14. How are employee lists searched?**  
`GET /users` supports search, paging, and sorting via `PageRequests.of(...)` and `EmployeeSpecification`.

## Accounts and transactions

**15. How are accounts opened?**  
`AccountService.openAccount` creates the account using the active account policy, generates an account number, and records an opening transaction if needed.

**16. What is the account policy feature?**  
`/account-policies` lets the app create and fetch active policies by account type and currency.

**17. How do deposit, withdraw, and transfer work?**  
They validate inputs, update balances, record transactions, and publish notification events.

**18. Why are transfers locked in ascending account-id order?**  
To reduce deadlock risk when two accounts are updated in one transaction.

**19. How are transactions stored?**  
Every money movement is persisted through `TransactionService.record(...)` into the transaction table.

**20. How are account lists and transaction lists paginated?**  
Controllers build `Pageable` objects using `PageRequests.of(...)` with allowed sort fields.

## Notifications and Kafka

**21. Why is Kafka used?**  
Kafka is used for asynchronous side effects, especially notification emails, so banking operations stay fast and transactional.

**22. Is `app.kafka.notification-topic` a queue?**  
Functionally yes for this app, but technically it is a Kafka topic used as an event stream.

**23. What does the notification publisher do?**  
`NotificationEventPublisher` creates a `BankActionEvent` and sends it to Kafka.

**24. What does the notification consumer do?**  
`NotificationEventConsumer` listens to Kafka, composes an email, and sends it through the configured mail service.

**25. Why should Kafka not wrap core balance updates?**  
Balance updates must remain synchronous and consistent. Kafka should handle only side effects like notifications, audit events, or downstream sync jobs.

## Frontend

**26. How is the frontend organized?**  
Angular standalone components are grouped by feature: auth, customers, employees, accounts, dashboard, profile, and management.

**27. How does the frontend enforce roles?**  
The `Auth` service stores roles from login and helper methods like `hasAnyRole(...)` control route access and UI visibility.

**28. Where is the API base URL configured?**  
In `src/app/core/config/api-config.ts`, which reads from the environment files.

**29. How are API failures shown to the user?**  
Dialogs and pages use a shared `apiErrorMessage(...)` utility plus notification toasts.

## Observability and runtime

**30. Where are logs written on Open Liberty?**  
The main runtime log is `messages.log`; application file logging can be configured separately through Logback.

**31. How do you debug SQL activity?**  
Hibernate SQL and bind logging are enabled through `application.properties` log levels.

## Good interview follow-up questions

- Why did you choose JWT instead of server sessions?
- Why did you keep balance updates synchronous but notifications async?
- How would you add a new role-management screen safely?
- How would you make notification delivery reliable across Kafka failures?
- How would you add audit history for money movement?

