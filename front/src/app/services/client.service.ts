import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { Client } from '../models/client.model';
import { ClientCreateRequest } from '../models/requests.model';
import { STORAGE_KEYS } from '../core/utils/constants';

@Injectable({ providedIn: 'root' })
export class ClientService {
  private base = `${environment.apiUrl}/clients`;
  constructor(private http: HttpClient) {}

  create(req: ClientCreateRequest): Observable<Client> {
    return this.http.post<Client>(this.base, req);
  }
  getById(id: string): Observable<Client> {
    return this.http.get<Client>(`${this.base}/${id}`);
  }

  list(): Observable<Client[]> {
    return this.http.get<Client[]>(this.base);
  }

  setSelectedClientId(id: string) {
    localStorage.setItem(STORAGE_KEYS.CLIENT_ID, id);
  }

  getSelectedClientId(): string | null {
    return localStorage.getItem(STORAGE_KEYS.CLIENT_ID);
  }

  clearSelectedClient() {
    localStorage.removeItem(STORAGE_KEYS.CLIENT_ID);
  }
}
