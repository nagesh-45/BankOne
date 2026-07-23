import { AccessLevel } from './create-user-request';

export interface UpdateUserRequest {
  firstName: string;
  lastName: string;
  email: string;
  enabled: boolean;
  accessLevel: AccessLevel;
}
