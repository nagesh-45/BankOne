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
        component: Dashboard
      },
      {
        path: 'profile',
        component: Profile
      },
      {
        path: 'profile/password',
        component: ChangePassword
      },
      {
        path: 'customers',
        component: CustomerList,
        canActivate: [authGuard],
        data: {
          roles: ['ADMIN', 'EMPLOYEE', 'MANAGER']
        }
      },
      {
        path: 'customers/:id',
        component: CustomerDetail,
        canActivate: [authGuard],
        data: {
          roles: ['ADMIN', 'EMPLOYEE', 'MANAGER']
        }
      },
      {
        path: 'accounts',
        component: AccountList,
        canActivate: [authGuard],
        data: {
          roles: ['ADMIN', 'EMPLOYEE', 'MANAGER']
        }
      },
      {
        path: 'employees',
        component: EmployeeList,
        canActivate: [authGuard],
        data: {
          roles: ['ADMIN']
        }
      },
      {
        path: 'management',
        component: Management,
        canActivate: [authGuard],
        data: {
          roles: ['ADMIN', 'EMPLOYEE']
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
