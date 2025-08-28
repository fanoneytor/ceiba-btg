import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Fund } from '../../models/fund.model';
import { Client } from '../../models/client.model';
import { FundService } from '../../services/fund.service';
import { ClientService } from '../../services/client.service';
import { TransactionService } from '../../services/transaction.service';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ConfirmationDialogComponent } from '../../shared/components/confirmation-dialog/confirmation-dialog.component';

@Component({
  standalone: true,
  selector: 'app-funds',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatDialogModule,
  ],
  templateUrl: './funds.component.html',
  styleUrls: ['./funds.component.scss'],
})
export class FundsComponent implements OnInit {
  funds: Fund[] = [];
  client: Client | null = null;
  loading = true;

  clientForm = this.fb.group({
    name: ['', [Validators.required]],
    email: [''],
    phone: [''],
    initialBalance: [500000],
    preferredNotification: ['EMAIL'],
  });

  subForm = this.fb.group({
    amount: [0, [Validators.required, Validators.min(1)]],
  });
  selectedFund: Fund | null = null;

  constructor(
    private fb: FormBuilder,
    private fundsApi: FundService,
    private clientsApi: ClientService,
    private txApi: TransactionService,
    private snack: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit() {
    this.loadFunds();
    this.clientsApi.selectedClient$.subscribe(client => {
      this.client = client;
    });
  }

  private loadFunds() {
    this.fundsApi.list().subscribe({
      next: (data) => {
        this.funds = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  createClient() {
    if (this.clientForm.invalid) return;
    this.clientsApi.create(this.clientForm.value as any).subscribe((c) => {
      this.snack.open('Cliente creado', 'Cerrar', { duration: 3000, panelClass: ['success-toast'] });
    });
  }

  openSubscribe(f: Fund) {
    this.selectedFund = f;
    const min = f.minimumAmount || 1;
    this.subForm.reset({ amount: min });
    this.subForm
      .get('amount')
      ?.setValidators([Validators.required, Validators.min(min)]);
    this.subForm.get('amount')?.updateValueAndValidity();
  }

  confirmSubscribe() {
    if (!this.client || !this.selectedFund || this.subForm.invalid) return;
    const amount = this.subForm.value.amount!;
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: { message: `¿Está seguro que desea suscribirse a ${this.selectedFund.name} con un monto de ${amount}?` },
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.txApi
          .subscribe({
            clientId: this.client!.id,
            fundId: this.selectedFund!.id,
            amount,
          })
          .subscribe((tx) => {
            this.snack.open(tx.message || 'Suscripción creada', 'Cerrar', {
              duration: 3000,
              panelClass: ['success-toast']
            });
            this.clientsApi.refreshClients();
            this.selectedFund = null;
          });
      }
    });
  }

  isSubscribed(fund: Fund): boolean {
    if (!this.client) {
      return false;
    }
    return this.client.activeFunds.some(activeFund => activeFund.fundId === fund.id);
  }

  cancel(fundId: string) {
    if (!this.client) return;

    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: { message: '¿Está seguro que desea cancelar la suscripción a este fondo?' },
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.txApi.cancel({ clientId: this.client!.id, fundId }).subscribe((tx) => {
          this.snack.open(tx.message || 'Cancelado', 'Cerrar', { duration: 3000, panelClass: ['success-toast'] });
          this.clientsApi.refreshClients();
        });
      }
    });
  }
}