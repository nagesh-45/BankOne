import { Routes } from '@angular/router';
import { Login } from './features/login/login';
import { Dashboard } from './features/dashboard/dashboard';
import { CustomerList } from './features/customers/customer-list/customer-list';
import { CustomerDetail } from './features/customers/customer-detail/customer-detail';
import { EmployeeList } from './features/employees/employee-list/employee-list';
import { Management } from './features/management/management';
import { Profile } from './features/profile/profile';
import { ChangePassword } from './features/profile/change-password/change-password';
import { MainLayout } from './core/layout/main-layout/main-layout';
import { authGuard } from './core/guards/auth-guard';
import { portalGuard, staffAppGuard } from './core/guards/portal-guard';
import { AccountList } from './features/accounts/account-list/account-list';
import { AccountDetail } from './features/accounts/account-detail/account-detail';
import { RoleList } from './features/roles/role-list/role-list';
import { PortalLayout } from './features/portal/portal-layout/portal-layout';
import { MyAccounts } from './features/portal/my-accounts/my-accounts';
import { MyAccountDetail } from './features/portal/my-account-detail/my-account-detail';
import { PortalBeneficiaries } from './features/portal/portal-beneficiaries/portal-beneficiaries';
import { TransferApprovals } from './features/transfers/transfer-approvals/transfer-approvals';
import { Audit } from './features/audit/audit';
import { AccountPolicies } from './features/policies/account-policies/account-policies';
import { TransactionList } from './features/transactions/transaction-list/transaction-list';
import { Reports } from './features/reports/reports';

export const routes: Routes = [
  {
    path: '',
    component: Login
  },
  {
    path: 'portal',
    component: PortalLayout,
    canActivate: [authGuard, portalGuard],
    children: [
      {
        path: 'accounts',
        component: MyAccounts
      },
      {
        path: 'accounts/:id',
        component: MyAccountDetail
      },
      {
        path: 'beneficiaries',
        component: PortalBeneficiaries
      },
      {
        path: '',
        redirectTo: 'accounts',
        pathMatch: 'full'
      }
    ]
  },
  {
    path: 'app',
    component: MainLayout,
    canActivate: [authGuard, staffAppGuard],
    children: [
      {
        path: 'dashboard',
        component: Dashboard,
        data: { breadcrumb: 'Dashboard' }
      },
      {
        path: 'profile',
        component: Profile,
        data: { breadcrumb: 'Profile' }
      },
      {
        path: 'profile/password',
        component: ChangePassword,
        data: {
          breadcrumb: 'Change password',
          breadcrumbParents: [{ label: 'Profile', url: '/app/profile' }]
        }
      },
      {
        path: 'customers',
        component: CustomerList,
        canActivate: [authGuard],
        data: {
          accesses: ['CUSTOMERS_READ'],
          roles: ['ADMIN', 'EMPLOYEE', 'MANAGER'],
          breadcrumb: 'Customers'
        }
      },
      {
        path: 'customers/:id',
        component: CustomerDetail,
        canActivate: [authGuard],
        data: {
          accesses: ['CUSTOMERS_READ'],
          roles: ['ADMIN', 'EMPLOYEE', 'MANAGER'],
          breadcrumb: 'Customer',
          breadcrumbParents: [{ label: 'Customers', url: '/app/customers' }]
        }
      },
      {
        path: 'accounts',
        component: AccountList,
        canActivate: [authGuard],
        data: {
          accesses: ['ACCOUNTS_READ'],
          roles: ['ADMIN', 'EMPLOYEE', 'MANAGER', 'TELLER', 'AUDITOR'],
          breadcrumb: 'Accounts'
        }
      },
      {
        path: 'accounts/:id',
        component: AccountDetail,
        canActivate: [authGuard],
        data: {
          accesses: ['ACCOUNTS_READ'],
          roles: ['ADMIN', 'EMPLOYEE', 'MANAGER', 'TELLER', 'AUDITOR'],
          breadcrumb: 'Account',
          breadcrumbParents: [{ label: 'Accounts', url: '/app/accounts' }]
        }
      },
      {
        path: 'transactions',
        component: TransactionList,
        canActivate: [authGuard],
        data: {
          accesses: ['ACCOUNTS_READ'],
          roles: ['ADMIN', 'EMPLOYEE', 'MANAGER', 'TELLER', 'AUDITOR'],
          breadcrumb: 'Transactions'
        }
      },
      {
        path: 'reports',
        component: Reports,
        canActivate: [authGuard],
        data: {
          accesses: ['ACCOUNTS_READ', 'DASHBOARD'],
          roles: ['ADMIN', 'EMPLOYEE', 'MANAGER', 'TELLER', 'AUDITOR'],
          breadcrumb: 'Reports'
        }
      },
      {
        path: 'employees',
        component: EmployeeList,
        canActivate: [authGuard],
        data: {
          accesses: ['USERS_MANAGE'],
          roles: ['ADMIN'],
          breadcrumb: 'Employees'
        }
      },
      {
        path: 'roles',
        component: RoleList,
        canActivate: [authGuard],
        data: {
          accesses: ['ROLES_MANAGE'],
          roles: ['ADMIN'],
          breadcrumb: 'Roles'
        }
      },
      {
        path: 'transfer-approvals',
        component: TransferApprovals,
        canActivate: [authGuard],
        data: {
          accesses: ['ACCOUNTS_WRITE'],
          roles: ['ADMIN', 'EMPLOYEE', 'MANAGER'],
          breadcrumb: 'Transfer approvals'
        }
      },
      {
        path: 'audit',
        component: Audit,
        canActivate: [authGuard],
        data: {
          roles: ['ADMIN', 'MANAGER', 'AUDITOR'],
          breadcrumb: 'Audit'
        }
      },
      {
        path: 'policies',
        component: AccountPolicies,
        canActivate: [authGuard],
        data: {
          accesses: ['POLICIES_MANAGE', 'ACCOUNTS_READ', 'ACCOUNTS_WRITE'],
          roles: ['ADMIN', 'EMPLOYEE', 'MANAGER', 'TELLER', 'AUDITOR'],
          breadcrumb: 'Account policies'
        }
      },
      {
        path: 'management',
        component: Management,
        canActivate: [authGuard],
        data: {
          accesses: [
            'CUSTOMERS_WRITE',
            'USERS_MANAGE',
            'ROLES_MANAGE',
            'POLICIES_MANAGE',
            'ACCOUNTS_READ',
            'ACCOUNTS_WRITE'
          ],
          roles: ['ADMIN', 'EMPLOYEE', 'MANAGER', 'TELLER', 'AUDITOR'],
          breadcrumb: 'Management'
        }
      },
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      }
    ]
  },
  {
    path: '**',
    redirectTo: ''
  }
];
