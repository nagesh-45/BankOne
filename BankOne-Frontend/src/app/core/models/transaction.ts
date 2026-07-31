export interface Transaction {
  transactionId: number;
  accountId: number;
  accountNumber?: string | null;
  customerId?: number | null;
  customerName?: string | null;
  transactionType: 'CREDIT' | 'DEBIT';
  amount: number;
  balanceAfter: number;
  currencyCode: string;
  narration: string | null;
  createdAt: string;
  createdBy: string | null;
}
