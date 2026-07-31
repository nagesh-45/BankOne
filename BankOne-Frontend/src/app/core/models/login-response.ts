export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  roles: string[];
  accesses?: string[];
  customerId?: number | null;
  portalUser?: boolean;
  username: string;
  firstName?: string;
  lastName?: string;
  email?: string;
}
