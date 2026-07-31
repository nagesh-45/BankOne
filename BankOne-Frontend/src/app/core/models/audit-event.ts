export type AuditCategory =
  | 'AUTH'
  | 'CUSTOMER'
  | 'ACCOUNT'
  | 'TRANSFER'
  | 'STAFF'
  | 'ROLE'
  | 'POLICY'
  | 'PORTAL';

export interface AuditEvent {
  id: number;
  category: AuditCategory;
  action: string;
  actorUsername: string | null;
  actorUserId: number | null;
  targetType: string | null;
  targetId: string | null;
  summary: string;
  details: string | null;
  success: boolean;
  createdAt: string;
}
