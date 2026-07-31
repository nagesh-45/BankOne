export type UserType = 'EMPLOYEE' | 'CUSTOMER';
export type AccessLevel = 'ADMIN' | 'NORMAL';

export interface CreateUserRequest {
  userType: UserType;
  /** @deprecated prefer roleNames */
  accessLevel?: AccessLevel;
  /** @deprecated prefer roleNames */
  roleName?: string;
  /** One or more staff roles (e.g. EMPLOYEE + MANAGER). */
  roleNames?: string[];
  /** Required when userType is CUSTOMER */
  customerId?: number;
  username: string;
  password: string;
  firstName: string;
  lastName: string;
  email: string;
}
