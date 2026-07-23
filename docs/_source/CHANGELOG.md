# CHANGELOG

Append-only history of documentation-relevant product changes.

---

## 2026-07-23 — Opening deposit writes CREDIT ledger

| Field | Value |
|-------|-------|
| **Feature** | openAccount records CREDIT when openingDeposit > 0 (customer create + add account) |
| **Files Modified** | `AccountServiceImpl.openAccount` |
| **Classes Modified** | `AccountServiceImpl` |
| **Methods Modified** | `openAccount` |
| **Reason** | History missing for opening deposits |
| **Impact** | bank_transaction rows with narration "Opening deposit" |

---

## 2026-07-23 — Fix blank screen after login (breadcrumbs)

| Field | Value |
|-------|-------|
| **Feature** | Make `Breadcrumbs` resilient so `MainLayout` does not crash post-login |
| **Files Modified** | `BankOne-Frontend/src/app/shared/components/breadcrumbs/breadcrumbs.ts` |
| **Classes Modified** | `Breadcrumbs` |
| **Methods Modified** | `buildCrumbs`, `urlOf`, `resolveLabel`; `crumbs` `toSignal` initialValue |
| **Reason** | `toSignal({ initialValue: this.buildCrumbs() })` ran before router tree ready; `pathFromRoot`/`snapshot.url` could throw and blank the app |
| **Impact** | After login, MainLayout renders; breadcrumbs fall back to Dashboard on error |

---

----|-------|
| **Feature** | `GET /accounts/{accountId}` and `GET /accounts/{accountId}/transactions` |
| **Files Modified** | `AccountController.java`, `AccountService`/`Impl`, `TransactionService`/`Impl`, `TransactionResponse.java`; docs `_source` API_DOCUMENTATION, MODULES/Account, MODULES/Transaction, CALL_FLOW, FUNCTIONAL_SPECIFICATION, CHANGELOG |
| **Classes Modified** | `AccountController`, `AccountService`/`Impl`, `TransactionService`/`Impl`, `TransactionResponse` |
| **Methods Modified** | `getAccountById`, `getTransactions`, `TransactionService.getByAccountId` |
| **Reason** | Staff can load one account and its paged ledger without UI yet |
| **Impact** | List API implemented (Partial overall: no withdraw/transfer/UI; dashboard count still stub) |

---


## 2026-07-23 — Technology stack living document

| Field | Value |
|-------|-------|
| **Feature** | Add `TECH_STACK` inventory; keep in sync on dependency changes |
| **Files Modified** | `docs/_source/TECH_STACK.md`, `docs/_source/README.md`, `.cursor/rules/documentation-architect.mdc`, `CHANGELOG.md` |
| **Classes Modified** | N/A |
| **Methods Modified** | N/A |
| **Reason** | Single place listing all technologies/versions used in the project |
| **Impact** | Architect rule requires updating TECH_STACK when pom/package.json/deploy/auth/DB change |

---

## 2026-07-23 — Transaction ledger foundation + deposit CREDIT write

| Field | Value |
|-------|-------|
| **Feature** | Transaction module foundation; deposit posts CREDIT ledger row |
| **Files Modified** | `transaction/entity/Transaction.java`, `enums/TransactionType.java`, `repository/TransactionRepository.java`, `service/TransactionService.java`, `TransactionServiceImpl.java`; `AccountServiceImpl.java`; docs `_source` MODULES/Transaction, Account, FUNCTIONAL_SPECIFICATION, CALL_FLOW, DATABASE_SCHEMA, CHANGELOG |
| **Classes Modified** | `Transaction`, `TransactionType`, `TransactionRepository`, `TransactionService`/`Impl`, `AccountServiceImpl` |
| **Methods Modified** | `TransactionServiceImpl.record()`; `AccountServiceImpl.deposit()` |
| **Reason** | Start immutable money-movement history; deposit must not only mutate balances |
| **Impact** | `bank_transaction` table; list/API still stub; dashboard `todayTransactionCount` still 0; openAccount does not yet write CREDIT |

---

## 2026-07-23 — Rename BanOne-BackEnd → BankOne-BackEnd

| Field | Value |
|-------|-------|
| **Feature** | Fix misspelled backend folder name |
| **Files Modified** | Folder rename; scripts, README, docs, architect rule |
| **Classes Modified** | N/A |
| **Methods Modified** | N/A |
| **Reason** | Correct spelling to match BankOne branding |
| **Impact** | Re-open IntelliJ on `BankOne-BackEnd` |

---

## 2026-07-23 — Move backend into BankOne-BackEnd

| Field | Value |
|-------|-------|
| **Feature** | Relocate Maven `src` + `pom.xml` under `BankOne-BackEnd/` |
| **Files Modified** | `BankOne-BackEnd/**`, `scripts/redeploy-liberty.sh`, `README.md`, docs paths, architect rule |
| **Classes Modified** | N/A (path move only) |
| **Methods Modified** | N/A |
| **Reason** | Separate backend folder alongside `BankOne-Frontend` |
| **Impact** | Open IntelliJ on `BankOne-BackEnd`; redeploy script builds from new root |

---

## 2026-07-23 — Documentation formatting / alignment fix

| Field | Value |
|-------|-------|
| **Feature** | Restyle DOCX + PDF (margins, tables, headings, code blocks) |
| **Files Modified** | `BankOne/docs/**/*.docx`, `**/*.pdf`, `docs/_assets/*`, `scripts/regenerate-docs.sh`, `scripts/_polish_docx.py` |
| **Classes Modified** | N/A |
| **Methods Modified** | N/A |
| **Reason** | User reported poor alignment inside exported docs |
| **Impact** | Open refreshed PDF/DOCX; use regenerate script after content updates |

---

## 2026-07-23 — Docs format: Word + PDF in project folder

| Field | Value |
|-------|-------|
| **Feature** | Replace Markdown docs with DOCX/PDF; move under `BankOne/docs` |
| **Files Modified** | `BankOne/docs/**` (docx+pdf), removed workspace `/docs` markdown; `.cursor/rules/documentation-architect.mdc`; `BankOne/README.md` |
| **Classes Modified** | N/A |
| **Methods Modified** | N/A |
| **Reason** | User requested PDF/Word instead of MD, located in the project folder |
| **Impact** | Architect rule treats `.docx` as editable source and `.pdf` as distribution copy |

---

## 2026-07-23 — Documentation baseline (Software Architect)

| Field | Value |
|-------|-------|
| **Feature** | Establish living `/docs` synchronized with codebase |
| **Files Modified** | `docs/**`, `.cursor/rules/documentation-architect.mdc` |
| **Classes Modified** | N/A (docs only) |
| **Methods Modified** | N/A |
| **Reason** | Permanent architect mandate: docs are part of source; capture current implemented state |
| **Impact** | All future features must update these docs + append this changelog |

---

## 2026-07-23 — Account policies GET + opening deposit UX

| Field | Value |
|-------|-------|
| **Feature** | Policy-aware opening deposit for Current accounts |
| **Files Modified** | Backend: `AccountPolicyController`, `AccountPolicyService`/`Impl`; Frontend: `account-policy.ts`, `opening-deposit-dialog/*`, `customer-detail.ts` |
| **Classes Modified** | `AccountPolicyController`, `AccountPolicyServiceImpl`, Angular `OpeningDepositDialog`, `CustomerDetail` |
| **Methods Modified** | `getActivePolicy()`, `addCurrentAccount()`, dialog submit → `openAccount` |
| **Reason** | CURRENT/INR policy requires min opening deposit (seed 5000); UI must not hardcode `1` |
| **Impact** | Customer detail Current flow; account open validation; docs `MODULES/Account.md`, API, sequences |

---

## 2026-07-22 — Accounts list + deposit API

| Field | Value |
|-------|-------|
| **Feature** | Accounts search UI + `POST /accounts/{id}/deposit` |
| **Files Modified** | `AccountController`, `AccountServiceImpl`, `DepositRequest`, frontend `features/accounts/*`, sidebar route |
| **Classes Modified** | `AccountController`, `AccountServiceImpl`, `AccountList` |
| **Methods Modified** | `searchAccounts()`, `deposit()` |
| **Reason** | Staff can find accounts and credit balances |
| **Impact** | No ledger rows yet; Transaction module still stub |

---

## 2026-07-23 — System-wide breadcrumbs

| Field | Value |
|-------|-------|
| **Feature** | Breadcrumbs in main layout from route `data.breadcrumb` / `data.breadcrumbParents` |
| **Files Modified** | Frontend: `shared/breadcrumbs/*`, `layout/main-layout/*`, feature `*.routes.ts` (breadcrumb metadata) |
| **Classes Modified** | `Breadcrumbs`, `MainLayout` |
| **Methods Modified** | Breadcrumb build from `ActivatedRoute` snapshot / router events |
| **Reason** | Staff navigation context across Customers, Accounts, Users, Dashboard |
| **Impact** | All authenticated shell pages show trail; leaf routes declare parents for nested screens |

