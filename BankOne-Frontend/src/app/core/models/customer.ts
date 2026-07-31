export interface Customer {
  customerId: number;
  customerCode?: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  dateOfBirth: string | null;
  address: string;
  status: string;
  transferApprovalThreshold?: number | null;
  createdAt: string;
  updatedAt: string;
}
