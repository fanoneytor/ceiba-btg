import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { Transaction } from '../models/transaction.model';
import {
  CancellationRequest,
  SubscriptionRequest,
} from '../models/requests.model';

@Injectable({ providedIn: 'root' })
export class TransactionService {
  private base = `${environment.apiUrl}/transactions`;
  constructor(private http: HttpClient) {}

  subscribe(req: SubscriptionRequest): Observable<Transaction> {
    return this.http.post<Transaction>(`${this.base}/subscribe`, req);
  }

  cancel(req: CancellationRequest): Observable<Transaction> {
    return this.http.post<Transaction>(`${this.base}/cancel`, req);
  }

  history(clientId: string) {
    return this.http.get<Transaction[]>(`${this.base}/history/${clientId}`);
  }
}
