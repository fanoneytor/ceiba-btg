export type TransactionType = 'SUBSCRIPTION' | 'CANCELLATION';
export type TransactionStatus = 'SUCCESS' | 'FAILED';

export interface Transaction {
  id: string;
  transactionId: string;
  clientId: string;
  fundId: string;
  type: TransactionType;
  amount: number;
  status: TransactionStatus;
  message: string;
  date: string; // ISO
}
