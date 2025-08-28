import { ActiveSubscription } from './active-subscription.model';

export type NotificationChannel = 'EMAIL' | 'SMS';

export interface Client {
  id: string;
  name: string;
  email: string | null;
  phone: string | null;
  availableBalance: number;
  preferredNotification: NotificationChannel;
  activeFunds: ActiveSubscription[];
  createdAt?: string;
  updatedAt?: string;
}
