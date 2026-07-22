export type UserType = 'EMPLOYEE' | 'CUSTOMER';
export type AccessLevel = 'ADMIN' | 'NORMAL';

export interface CreateUserRequest {
  userType: UserType;
  accessLevel?: AccessLevel;
  username: string;
  password: string;
  firstName: string;
  lastName: string;
  email: string;
}
