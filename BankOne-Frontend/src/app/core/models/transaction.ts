export interface Transaction {
  transactionId: number;
  accountId: number;
  transactionType: 'CREDIT' | 'DEBIT';
  amount: number;
  balanceAfter: number;
  currencyCode: string;
  narration: string | null;
  createdAt: string;
  createdBy: string | null;
}
