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
import { Observable } from 'rxjs';

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
  clients$: Observable<Client[]>;
  selectedClientId: string | null = null;

  constructor(private clientApi: ClientService) {
    this.clients$ = this.clientApi.clients$;
  }

  ngOnInit() {
    this.clientApi.selectedClient$.subscribe(client => {
      this.selectedClientId = client ? client.id : null;
    });
  }

  onSelectClient(id: string) {
    if (id) {
      this.clientApi.setSelectedClientId(id);
    } else {
      this.clientApi.clearSelectedClient();
    }
  }

  trackById = (_: number, c: Client) => c.id;
}