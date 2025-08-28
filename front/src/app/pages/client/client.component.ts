import { Component, OnInit } from '@angular/core';
import { ClientService } from '../../services/client.service';
import { TransactionService } from '../../services/transaction.service';
import { Client } from '../../models/client.model';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';

@Component({
  standalone: true,
  selector: 'app-client',
  imports: [CommonModule, MatCardModule, MatTableModule, MatButtonModule],
  templateUrl: './client.component.html',
  styleUrls: ['./client.component.scss'],
})
export class ClientComponent implements OnInit {
  client: Client | null = null;

  constructor(
    private clientsApi: ClientService,
    private txApi: TransactionService,
    private snack: MatSnackBar
  ) {}

  ngOnInit() {
    this.reload();
  }

  reload() {
    const id = this.clientsApi.getSelectedClientId();
    if (!id) return;
    this.clientsApi.getById(id).subscribe((c) => (this.client = c));
  }

  cancel(fundId: string) {
    if (!this.client) return;
    this.txApi.cancel({ clientId: this.client.id, fundId }).subscribe((tx) => {
      this.snack.open(tx.message || 'Cancelled', 'Close', { duration: 3000 });
      this.reload();
    });
  }
}
