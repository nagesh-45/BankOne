export interface CreateCustomerRequest {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  dateOfBirth: string | null;
  address: string;
  status: string;
  branchCode: string;
  accountType: string;
  currencyCode: string;
  openingDeposit: number;
  /** Optional — creates CUSTOMER portal login in the same request */
  portalUsername?: string;
  portalPassword?: string;
}
