import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { BankComponent } from './bank/bank.component';
import { LoginComponent } from './login/login.component';
import { MainBodyComponent } from './main-body/main-body.component';
import { VoucherBankComponent } from './voucher-bank/voucher-bank.component';

const routes: Routes = [
{
    path: '',
    children:[
      {
        path: '',
        component: MainBodyComponent,
        children:[
          {
            path: 'voucher-bank',
            component: VoucherBankComponent
          },
          {
            path: 'bank',
            component: BankComponent
          }
        ]
      },
      {
        path: 'login',
        component: LoginComponent
      }
    ]
  }
  
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
