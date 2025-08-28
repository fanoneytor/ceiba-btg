import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { FundsComponent } from './pages/funds/funds.component';
import { ClientComponent } from './pages/client/client.component';
import { HistoryComponent } from './pages/history/history.component';

const routes: Routes = [
  { path: '', redirectTo: 'funds', pathMatch: 'full' },
  { path: 'funds', component: FundsComponent },
  { path: 'client', component: ClientComponent },
  { path: 'history', component: HistoryComponent },
  { path: '**', redirectTo: 'funds' },
];

@NgModule({ imports: [RouterModule.forRoot(routes)], exports: [RouterModule] })
export class AppRoutingModule {}
