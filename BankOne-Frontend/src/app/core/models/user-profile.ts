export interface UserProfile {
  userId: number;
  employeeCode?: string;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  enabled: boolean;
  roles: string[];
  lastLogin: string | null;
}
