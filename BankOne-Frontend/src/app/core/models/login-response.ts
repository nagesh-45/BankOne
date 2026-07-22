export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  roles: string[];
  username: string;
  firstName?: string;
  lastName?: string;
  email?: string;
}
