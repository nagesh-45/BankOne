# Class Diagrams

## Package overview

```
classDiagram
  direction TB
  class AuthenticationController
  class AuthenticationService
  class JwtService
  class JwtAuthenticationFilter
  class CustomUserDetailsService
  class BankUserDetails

  AuthenticationController --> AuthenticationService
  AuthenticationService --> JwtService
  AuthenticationService --> CustomUserDetailsService
  JwtAuthenticationFilter --> JwtService
  JwtAuthenticationFilter --> CustomUserDetailsService
  CustomUserDetailsService --> BankUserDetails
```

## Account domain

```
classDiagram
  direction TB
  class AccountController {
    +openAccount()
    +searchAccounts()
    +getAccountsByCustomer()
    +updateAccountStatus()
    +deposit()
  }
  class AccountService
  class AccountServiceImpl {
    +openAccount()
    +deposit()
    +searchAccounts()
    +updateAccountStatus()
  }
  class AccountRepository
  class Account
  class AccountNumberGenerator
  class AccountSpecification
  class AccountPolicyController {
    +createPolicy()
    +getActivePolicy()
  }
  class AccountPolicyService
  class AccountPolicyServiceImpl
  class AccountPolicyRepository
  class AccountPolicy
  class Customer

  AccountController --> AccountService
  AccountService <|.. AccountServiceImpl
  AccountServiceImpl --> AccountRepository
  AccountServiceImpl --> AccountPolicyRepository
  AccountServiceImpl --> AccountNumberGenerator
  AccountServiceImpl --> Customer
  AccountRepository --> Account
  AccountSpecification ..> Account
  Account --> Customer : customer_id
  AccountPolicyController --> AccountPolicyService
  AccountPolicyService <|.. AccountPolicyServiceImpl
  AccountPolicyServiceImpl --> AccountPolicyRepository
  AccountPolicyRepository --> AccountPolicy
```

## Customer domain

```
classDiagram
  direction TB
  class CustomerController
  class CustomerService
  class CustomerServiceImpl {
    +createCustomer()
    +updateCustomer()
    +searchCustomers()
    +deleteCustomer()
  }
  class CustomerRepository
  class Customer
  class AccountService

  CustomerController --> CustomerService
  CustomerService <|.. CustomerServiceImpl
  CustomerServiceImpl --> CustomerRepository
  CustomerServiceImpl --> AccountService
  CustomerRepository --> Customer
```

## User / role domain

```
classDiagram
  direction TB
  class UserController
  class UserService
  class User
  class Role
  class UserRole
  class UserRepository
  class RoleRepository
  class UserRoleRepository

  UserController --> UserService
  UserService --> UserRepository
  UserService --> UserRoleRepository
  UserService --> RoleRepository
  User --> UserRole
  Role --> UserRole
```

## Shared

```
classDiagram
  direction TB
  class AuditableEntity {
    createdAt
    updatedAt
    createdBy
    updatedBy
    version
  }
  class AccountPolicy
  class User
  class Role
  class UserRole
  class SecurityConfig
  class GlobalExceptionHandler

  AuditableEntity <|-- AccountPolicy
  AuditableEntity <|-- User
  AuditableEntity <|-- Role
  AuditableEntity <|-- UserRole
```

## Frontend feature map (logical)

```
flowchart LR
  Login --> AuthService
  Dashboard --> DashboardService
  Customers --> CustomerService
  Accounts --> AccountService
  OpeningDeposit --> AccountPolicyService
  Employees --> UserService
  AuthService --> API
  CustomerService --> API
  AccountService --> API
  AccountPolicyService --> API
  UserService --> API
  DashboardService --> API
```
