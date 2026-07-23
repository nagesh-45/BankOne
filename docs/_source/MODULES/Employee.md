# Employee

## 1. Feature Overview

ADMIN management of staff users (employees): list, create, update,
including roles used by Spring Security.

**Status:** Implemented (backend package name `user`; UI label
Employees)

## 2. Business Purpose

Provision bank staff who can log in and operate customer/account
functions according to role.

## 3. User Workflow

1.  Sidebar → Employees (`/app/employees`) --- ADMIN only
2.  List / create / edit via dialogs
3.  Assigned roles control API and route access

## 4. Execution Flow

    UserController.*
            ↓
    UserService.*
            ↓
    UserRepository / UserRoleRepository / RoleRepository

## 5. Database Tables

`users`, `roles`, `user_roles`

## 6. REST APIs

  --------------------------------------------------------------
  Method               Path                 Roles
  -------------------- -------------------- --------------------
  GET                  `/users`             ADMIN

  POST                 `/users`             ADMIN

  PUT                  `/users/{id}`        ADMIN
  --------------------------------------------------------------

## 7. Controllers

`com.bankone.user.controller.UserController`

## 8. Services

`UserService` (+ `AdminRoleInitializer` for bootstrap admin)

## 9. Repositories

`UserRepository`, `UserRoleRepository`, `RoleRepository`

## 10. DTOs

Request/response DTOs under `com.bankone.user.dto` (as present in
codebase)

## 11. Entities

`User`, `Role`, `UserRole`

## 12. Utility Classes

`EmployeeSpecification` for search

## 13. Configuration Classes

`SecurityConfig` `/users/**` → `hasRole("ADMIN")`\
`RoleInitializer` seeds role names including unused TELLER, AUDITOR,
CUSTOMER

## 14. Validation Rules

Password encoding BCrypt on create/update; field validation per DTOs

## 15. Security Rules

ADMIN only for all `/users` APIs and Angular route

## 16. Exception Handling

Duplicate username / missing role → business exceptions

## 17. Logging

Security TRACE may log auth for employees in dev

## 18. Audit Events

`AuditableEntity` fields on user/role join entities

## 19. Testing Strategy

- Non-ADMIN gets 403 on `/users`
- Create employee → login works
- Disable user → login blocked

## 20. Future Extension Guide

- Self-service profile already under auth; keep employee admin separate
- Activate TELLER/AUDITOR in SecurityConfig when features land
- Branch assignment when Branch module exists

------------------------------------------------------------------------

# Future Modification Guide

### Requirement: Allow MANAGER to list employees (read-only)

  ------------------------------------------------------------
  Item                      Detail
  ------------------------- ----------------------------------
  Files                     `SecurityConfig.java`,
                            `UserController` (optional
                            `@PreAuthorize`), `app.routes.ts`,
                            sidebar

  Methods                   `securityFilterChain` matchers for
                            GET `/users/**`

  Impact                    Data exposure review
  ------------------------------------------------------------

### Requirement: Add mandatory employee code format

  ------------------------------------------------------------
  Item                      Detail
  ------------------------- ----------------------------------
  Files                     User entity/DTO, `UserService`
                            create/update, Angular employee
                            dialog

  Impact                    Login still uses username; display
                            uses employeeCode
  ------------------------------------------------------------

### Call hierarchy (create)

    UserController.create...
            ↓
    UserService.create...
            ↓
    PasswordEncoder.encode()
            ↓
    UserRepository.save()
            ↓
    UserRoleRepository.save()
