export interface Account {
  accountId: number;
  accountNumber: string;
  branchCode: string;
  accountType: string;
  ordinal: number;
  currencyCode: string;
  checkDigit: number;
  availableBalance: number;
  ledgerBalance: number;
  status: string;
  createdAt: string;
  activatedAt: string | null;
  createdBy: string | null;
  customerId: number;
  customerCode?: string;
  customerName?: string;

}
