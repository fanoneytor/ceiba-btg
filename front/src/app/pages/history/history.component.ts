import { Component, OnInit } from '@angular/core';
import { TransactionService } from '../../services/transaction.service';
import { ClientService } from '../../services/client.service';
import { Transaction } from '../../models/transaction.model';

import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';

@Component({
  standalone: true,
  selector: 'app-history',
  imports: [CommonModule, MatCardModule, MatTableModule],
  templateUrl: './history.component.html',
  styleUrls: ['./history.component.scss'],
})
export class HistoryComponent implements OnInit {
  data: Transaction[] = [];
  constructor(
    private txApi: TransactionService,
    private clientsApi: ClientService
  ) {}
  ngOnInit() {
    const id = this.clientsApi.getSelectedClientId();
    if (!id) return;
    this.txApi.history(id).subscribe((res) => (this.data = res));
  }
}
