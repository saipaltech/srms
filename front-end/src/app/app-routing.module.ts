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
import { VoucherBankOffComponent } from './voucher-bank-off/voucher-bank.component';
import { NewNavComponent } from './new-nav/new-nav.component';
import { ChequeEntryComponent } from './cheque-entry/cheque-entry.component';
import { EditVoucherComponent } from './edit-voucher/edit-voucher.component';
import { VoucherTransferComponent } from './voucher-transfer/voucher-transfer.component';
import { DayCloseComponent } from './day-close/day-close.component';
import { DayCloseChequeComponent } from './day-close-cheque/day-close-cheque.component';
import { ChequeReportComponent } from './cheque-report/cheque-report.component';
import { NewDesignComponent } from './new-design/new-design.component';
import { NewMainBodyComponent } from './new-main-body/new-main-body.component';
import { NiceAdminComponent } from './nice-admin/nice-admin.component';
import { FrontEndPasswordChangeComponent } from './front-end-password-change/front-end-password-change.component';
import { ForgetPassComponent } from './login/forget-pass.component';
import { SettingsComponent } from './settings/settings.component';
import { AllUsersComponent } from './all-users/all-users.component';
import { FileUploadComponent } from './file-upload/file-upload.component';
import { RevenueReportComponent } from './revenue-report/revenue-report.component';
import { VerifyVoucherPortalComponent } from './verify-voucher-portal/verify-voucher-portal.component';
import { ChequeClearComponent } from './cheque-clear/cheque-clear.component';
import { VoucherCancelComponent } from './voucher-cancel/voucher-cancel.component';
import { VerifyDirectBankDepositComponent } from './verify-direct-deposit/verify-direct-deposit.component';

const routes: Routes = [
{
    path: '',
    children:[
      {
        path: '',
        component: NewMainBodyComponent,
        canActivate:[AuthGuard],
        children:[
          {
            path: 'voucher-bank',
            component: VoucherBankComponent
          },
          {
            path: 'voucher-bank-off',
            component: VoucherBankOffComponent
          },
          {
            path: 'cheque-entry',
            component: ChequeEntryComponent
          },
          {
            path: 'edit-voucher',
            component: EditVoucherComponent
          },
          {
            path: 'voucher-transfer',
            component: VoucherTransferComponent
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
            path: 'verify-voucher-portal',
            component: VerifyVoucherPortalComponent
          },
          {
            path: 'clear-cheque',
            component: ChequeClearComponent
          },
          {
            path: 'voucher-cancel',
            component: VoucherCancelComponent
          },
          {
            path: 'verify-direct-deposit',
            component: VerifyDirectBankDepositComponent
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
          {
            path: 'revenue-report',
            component: RevenueReportComponent
          },
          {
            path: 'day-close',
            component: DayCloseComponent
          },
          {
            path: 'day-close-cheque',
            component: DayCloseChequeComponent
          },
          {
            path: 'settings',
            component: SettingsComponent
          },
          {
            path: 'all-users',
            component: AllUsersComponent
          },
          {
            path: 'user-import',
            component: FileUploadComponent
          },
          
        ]
      },
      {
        path: 'login',        
        component: LoginComponent,
        canActivate:[LoginGuard]
      },
      {
        path: 'new-nav',        
        component: NewNavComponent,
      },
      {
        path: 'report-generate',
        component: TrialComponent
      },
      {
        path: 'cheque-report',
        component: ChequeReportComponent
      },
      {
        path: 'password-change',
        component: FrontEndPasswordChangeComponent
      },
      {
        path: 'forget-password',
        component: ForgetPassComponent
      },
 
    ]
  },
  {
    path:"**",
    redirectTo:"/"
  },/*{
    path: "password-change",
    redirectTo:"/password-change"
  },
  {
    path: 'forget-password',
    redirectTo:"/forget-password"
  },*/
  
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
