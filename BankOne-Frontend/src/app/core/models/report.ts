export interface NamedCount {
  name: string;
  count: number;
}

export interface DailyTrendPoint {
  date: string;
  creditAmount: number;
  debitAmount: number;
  creditCount: number;
  debitCount: number;
}

export interface TransactionTrendsReport {
  fromDate: string;
  toDate: string;
  daily: DailyTrendPoint[];
  totalCreditAmount: number;
  totalDebitAmount: number;
  totalCreditCount: number;
  totalDebitCount: number;
}

export interface AccountMixReport {
  byType: NamedCount[];
  byStatus: NamedCount[];
  totalAccounts: number;
}

export interface ApprovalsReport {
  fromDate: string;
  toDate: string;
  byStatus: NamedCount[];
  byStaff: NamedCount[];
  totalRequests: number;
}

export type ReportCategory = 'transaction-trends' | 'account-mix' | 'approvals';
