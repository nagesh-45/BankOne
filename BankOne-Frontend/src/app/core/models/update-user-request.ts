import { AccessLevel } from './create-user-request';

export interface UpdateUserRequest {
  firstName: string;
  lastName: string;
  email: string;
  enabled: boolean;
  /** @deprecated prefer roleNames */
  accessLevel?: AccessLevel;
  /** @deprecated prefer roleNames */
  roleName?: string;
  /** Replace staff roles with this set. */
  roleNames?: string[];
}
