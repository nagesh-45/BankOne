export interface AccountPolicy {
  policyId: number;
  accountType: string;
  currencyCode: string;
  openingDepositRequired: boolean;
  requiredOpeningDeposit: number;
  minimumBalance: number;
  active: boolean;
  effectiveFrom: string;
  effectiveTo: string | null;
}
