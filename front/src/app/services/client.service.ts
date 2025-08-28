import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Client } from '../models/client.model';
import { ClientCreateRequest } from '../models/requests.model';
import { STORAGE_KEYS } from '../core/utils/constants';

@Injectable({ providedIn: 'root' })
export class ClientService {
  private base = `${environment.apiUrl}/clients`;

  private clientsSubject = new BehaviorSubject<Client[]>([]);
  clients$ = this.clientsSubject.asObservable();

  private selectedClientSubject = new BehaviorSubject<Client | null>(null);
  selectedClient$ = this.selectedClientSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadInitialData();
  }

  private loadInitialData() {
    this.list().subscribe();
    const selectedId = this.getSelectedClientId();
    if (selectedId) {
      this.getById(selectedId).subscribe();
    }
  }

  create(req: ClientCreateRequest): Observable<Client> {
    return this.http.post<Client>(this.base, req).pipe(
      tap(client => {
        this.setSelectedClientId(client.id);
        this.refreshClients();
        this.selectedClientSubject.next(client);
      })
    );
  }

  getById(id: string): Observable<Client> {
    return this.http.get<Client>(`${this.base}/${id}`).pipe(
      tap(client => this.selectedClientSubject.next(client))
    );
  }

  list(): Observable<Client[]> {
    return this.http.get<Client[]>(this.base).pipe(
      tap(clients => this.clientsSubject.next(clients))
    );
  }

  refreshClients() {
    this.list().subscribe();
    const selectedId = this.getSelectedClientId();
    if (selectedId) {
      this.getById(selectedId).subscribe();
    }
  }

  setSelectedClientId(id: string) {
    localStorage.setItem(STORAGE_KEYS.CLIENT_ID, id);
    this.getById(id).subscribe();
  }

  getSelectedClientId(): string | null {
    return localStorage.getItem(STORAGE_KEYS.CLIENT_ID);
  }

  clearSelectedClient() {
    localStorage.removeItem(STORAGE_KEYS.CLIENT_ID);
    this.selectedClientSubject.next(null);
  }
}