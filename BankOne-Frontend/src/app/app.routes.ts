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
import { AccountList } from './features/accounts/account-list/account-list';
import { AccountDetail } from './features/accounts/account-detail/account-detail';

export const routes: Routes = [
  {
    path: '',
    component: Login
  },
  {
    path: 'app',
    component: MainLayout,
    canActivate: [authGuard],
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
          roles: ['ADMIN', 'EMPLOYEE', 'MANAGER'],
          breadcrumb: 'Customers'
        }
      },
      {
        path: 'customers/:id',
        component: CustomerDetail,
        canActivate: [authGuard],
        data: {
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
          roles: ['ADMIN', 'EMPLOYEE', 'MANAGER'],
          breadcrumb: 'Accounts'
        }
      },
      {
        path: 'accounts/:id',
        component: AccountDetail,
        canActivate: [authGuard],
        data: {
          roles: ['ADMIN', 'EMPLOYEE', 'MANAGER'],
          breadcrumb: 'Account',
          breadcrumbParents: [{ label: 'Accounts', url: '/app/accounts' }]
        }
      },
      {
        path: 'employees',
        component: EmployeeList,
        canActivate: [authGuard],
        data: {
          roles: ['ADMIN'],
          breadcrumb: 'Employees'
        }
      },
      {
        path: 'management',
        component: Management,
        canActivate: [authGuard],
        data: {
          roles: ['ADMIN', 'EMPLOYEE'],
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
