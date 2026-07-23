# Change Impact Guide

Use this when modifying an existing feature. Always pair with the
module's **Future Modification Guide**.

## Cross-cutting hotspots

  ---------------------------------------------------------------------------
  Change       Primary files                      Likely collateral
  ------------ ---------------------------------- ---------------------------
  New REST     `*Controller`, `SecurityConfig`    Frontend service, routes,
  endpoint                                        `API_DOCUMENTATION.md`,
                                                  redeploy Liberty

  New role     `SecurityConfig`, Angular          `Employee` role seeding
  permission   `authGuard` / sidebar              

  JWT behavior `JwtService`,                      Login UI storage,
               `JwtAuthenticationFilter`          interceptor

  Account      `AccountNumberGenerator`,          Existing numbers
  number       `AccountServiceImpl.openAccount`   (non-retroactive), tests
  format                                          

  Opening      `AccountPolicy*`,                  Opening-deposit dialog,
  deposit      `AccountServiceImpl.openAccount`   customer create flow
  rules                                           

  Balance      `AccountServiceImpl.deposit`       Future Transaction module,
  mutation     (today)                            dashboard counts

  Customer     `BusinessIdFormatter`,             All UI showing customer
  identity     `Customer.getCustomerCode`         code
  display                                         

  API base URL `api-config.ts`                    All HTTP calls
  ---------------------------------------------------------------------------

## Feature → impact matrix

  -----------------------------------------------------------------
  If you change...                  Also check...
  --------------------------------- -------------------------------
  Customer create with account      `CustomerServiceImpl`,
                                    `AccountService.openAccount`,
                                    policies

  Deposit                           Account balances only ---
                                    **no** transaction table yet;
                                    adding ledger will touch
                                    deposit + openAccount

  Account status                    Future status-transition rules;
                                    deposit currently requires
                                    ACTIVE

  Account policy seed               `AccountPolicyInitializer`; GET
                                    policy UI dialogs

  User/employee create              Roles, login, `/users` ADMIN
                                    gate

  Dashboard counts                  `DashboardServiceImpl`;
                                    transaction count still stub
  -----------------------------------------------------------------

## Frontend / backend pairs

  ------------------------------------------------------------
  Backend                      Frontend
  ---------------------------- -------------------------------
  `AuthenticationController`   `features/login`,
                               `core/services/auth.ts`

  `CustomerController`         `features/customers/*`

  `AccountController`          `features/accounts/*`,
                               customer-detail account actions

  `AccountPolicyController`    `account-policy.ts`,
                               `opening-deposit-dialog`

  `UserController`             `features/employees/*`

  `DashboardController`        `features/dashboard/*`
  ------------------------------------------------------------

## Documentation impact checklist

After any of the above, update:

1.  `docs/MODULES/<Module>.md`
2.  `docs/API_DOCUMENTATION.md` and/or `docs/DATABASE_SCHEMA.md` if
    surface changed
3.  `docs/CALL_FLOW.md` / `SEQUENCE_DIAGRAMS.md` for flow changes
4.  `docs/CHANGELOG.md` (append)
5.  `docs/FUNCTIONAL_SPECIFICATION.md` if capability status changes
