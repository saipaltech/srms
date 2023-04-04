import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ApproveVoucherComponent } from './approve-voucher/approve-voucher.component';
import { AuthGuard, LoginGuard } from './auth/auth.guard';
import { BankComponent } from './bank/bank.component';
import { BranchComponent } from './branch/branch.component';
import { LoginComponent } from './login/login.component';
import { MainBodyComponent } from './main-body/main-body.component';
import { ReportComponent } from './report/report.component';
import { TrialComponent } from './trial/trial.component';
import { UserProfileComponent } from './user-profile/user-profile.component';
import { UsersComponent } from './users/users.component';
import { VerifyVoucherComponent } from './verify-voucher/verify-voucher.component';
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
          },
          {
            path: 'verify-voucher',
            component: VerifyVoucherComponent
          },
          {
            path: 'user-profile',
            component: UserProfileComponent
          },
          {
            path: 'approve-voucher',
            component: ApproveVoucherComponent
          },
          {
            path: 'report',
            component: ReportComponent
          },
        ]
      },
      {
        path: 'login',        
        component: LoginComponent,
        canActivate:[LoginGuard]
      },
      {
        path: 'trial',
        component: TrialComponent
      },
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
