export interface AccessDefinition {
  code: string;
  label: string;
}

export interface RoleSummary {
  roleId: number;
  roleName: string;
  description: string | null;
  accessCodes: string[];
  systemRole: boolean;
}

export interface CreateRoleRequest {
  roleName: string;
  description?: string;
  accessCodes: string[];
}

export interface UpdateRoleRequest {
  description?: string | null;
  accessCodes: string[];
}
