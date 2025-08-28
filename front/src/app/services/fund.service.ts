import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { Fund } from '../models/fund.model';

@Injectable({ providedIn: 'root' })
export class FundService {
  private base = `${environment.apiUrl}/funds`;
  constructor(private http: HttpClient) {}
  list(): Observable<Fund[]> {
    return this.http.get<Fund[]>(this.base);
  }
}
