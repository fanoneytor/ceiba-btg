export interface ClientCreateRequest {
  name: string;
  email?: string;
  phone?: string;
  initialBalance?: number;
  preferredNotification?: 'EMAIL' | 'SMS';
}

export interface SubscriptionRequest {
  clientId: string;
  fundId: string;
  amount: number;
}

export interface CancellationRequest {
  clientId: string;
  fundId: string;
}
