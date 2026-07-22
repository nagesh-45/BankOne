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
}
