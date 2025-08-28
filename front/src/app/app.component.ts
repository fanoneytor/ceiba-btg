import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { ClientService } from './services/client.service';
import { Client } from './models/client.model';
import { Observable, tap } from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatButtonModule,
    MatFormFieldModule,
    MatSelectModule,
    MatIconModule,
  ],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit {
  clients$!: Observable<Client[]>;
  selectedClientId: string | null = null;

  constructor(private clientsApi: ClientService) {}

  ngOnInit() {
    this.selectedClientId = this.clientsApi.getSelectedClientId();
    this.clients$ = this.clientsApi.list().pipe(
      tap((list) => {
        if (!this.selectedClientId && list.length > 0) {
          this.selectedClientId = list[0].id;
          this.clientsApi.setSelectedClientId(this.selectedClientId);
        }
      })
    );
  }

  onSelectClient(id: string) {
    this.selectedClientId = id || null;
    if (id) this.clientsApi.setSelectedClientId(id);
    else this.clientsApi.clearSelectedClient();
  }

  trackById = (_: number, c: Client) => c.id;
}
