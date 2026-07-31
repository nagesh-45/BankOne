export type BeneficiaryBankType = 'SAME_BANK' | 'OTHER_BANK';

export interface Beneficiary {
  beneficiaryId: number;
  nickname: string;
  bankType: BeneficiaryBankType;
  accountNumber: string;
  accountHolderName: string;
  linkedAccountId: number | null;
  ifsc: string | null;
  bankName: string | null;
  createdAt: string;
}

export interface CreateBeneficiaryRequest {
  nickname: string;
  bankType: BeneficiaryBankType;
  accountNumber: string;
  accountHolderName: string;
  ifsc?: string;
  bankName?: string;
}

export interface PortalTransferRequest {
  amount: number;
  toAccountNumber?: string;
  beneficiaryId?: number;
  narration?: string;
}

export interface TransferOutcome {
  outcome: 'EXECUTED' | 'PENDING_APPROVAL';
  transferRequestId: number | null;
  status: string;
  fromAccountId: number;
  toAccountId: number | null;
  amount: number;
  bankType: BeneficiaryBankType;
  message: string;
  createdAt: string;
}

export interface PendingTransfer {
  transferRequestId: number;
  customerId: number;
  fromAccountId: number;
  toAccountId: number | null;
  beneficiaryId: number | null;
  amount: number;
  bankType: BeneficiaryBankType;
  status: string;
  destinationAccountNumber: string | null;
  accountHolderName: string | null;
  ifsc: string | null;
  bankName: string | null;
  narration: string | null;
  approvalReason: string | null;
  requestedBy: string | null;
  resolvedBy: string | null;
  rejectionReason: string | null;
  createdAt: string;
  resolvedAt: string | null;
}
