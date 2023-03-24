import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard, LoginGuard } from './auth/auth.guard';
import { BankComponent } from './bank/bank.component';
import { BranchComponent } from './branch/branch.component';
import { LoginComponent } from './login/login.component';
import { MainBodyComponent } from './main-body/main-body.component';
import { UsersComponent } from './users/users.component';
import { VoucherBankComponent } from './voucher-bank/voucher-bank.component';

const routes: Routes = [
{
    path: '',
    children:[
      {
        path: '',
        component: MainBodyComponent,
        canActivate:[AuthGuard],
        children:[
          {
            path: 'voucher-bank',
            component: VoucherBankComponent
          },
          {
            path: 'bank',
            component: BankComponent
          },
          {
            path: 'branch',
            component: BranchComponent
          },
          {
            path: 'users',
            component: UsersComponent
          }
        ]
      },
      {
        path: 'login',        
        component: LoginComponent,
        canActivate:[LoginGuard]
      }
    ]
  },
  {
    path:"**",
    redirectTo:"/login"
  }
  
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
