import { APP_INITIALIZER, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HeaderComponent } from './header/header.component';
import { NavbarComponent } from './navbar/navbar.component';
import { MainBodyComponent } from './main-body/main-body.component';
import { LoginComponent, TwoFaModalComponent } from './login/login.component';
import { VoucherBankComponent } from './voucher-bank/voucher-bank.component';

import { ToastrModule } from 'ngx-toastr';
import { NgSelectModule } from '@ng-select/ng-select';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BankComponent, BankUsersComponent } from './bank/bank.component';
import { HttpClientModule, HTTP_INTERCEPTORS, HttpClient  } from '@angular/common/http';
import { BranchComponent } from './branch/branch.component';
import { ChangePasswordComponent, UserLimitComponent, UsersComponent } from './users/users.component';
import { PaginationModule } from 'ngx-bootstrap/pagination';
import { AppConfig } from './app.config';
import { HashLocationStrategy, LocationStrategy } from '@angular/common';
import { AuthGuard, AuthorizeGuard, LoginGuard } from './auth/auth.guard';
import { AuthService } from './auth/auth.service';
import { ApiService } from './api.service';
import { AuthInterceptor } from './auth-interceptor';
import { VerifyVoucherComponent } from './verify-voucher/verify-voucher.component';
import { ModalModule } from 'ngx-bootstrap/modal';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { UserProfileComponent } from './user-profile/user-profile.component';
import { ApproveVoucherComponent } from './approve-voucher/approve-voucher.component';
import { ReportComponent } from './report/report.component';
import { TrialComponent } from './trial/trial.component';
import { VoucherBankOffComponent } from './voucher-bank-off/voucher-bank.component';
import { NewNavComponent } from './new-nav/new-nav.component';

import { ChequeEntryComponent } from './cheque-entry/cheque-entry.component';
import { TooltipModule } from 'ngx-bootstrap/tooltip';
import { EditVoucherComponent } from './edit-voucher/edit-voucher.component';
import { VoucherTransferComponent } from './voucher-transfer/voucher-transfer.component';
import { DayCloseComponent } from './day-close/day-close.component';
import { ChequeReportComponent } from './cheque-report/cheque-report.component';

import { BsDatepickerModule } from 'ngx-bootstrap/datepicker';
import { NewDesignComponent } from './new-design/new-design.component';
import { NewHeaderComponent } from './new-header/new-header.component';
import { NewSidebarComponent } from './new-sidebar/new-sidebar.component';
import { NewMainBodyComponent } from './new-main-body/new-main-body.component';
import { HttpInterceptorService } from './http-interceptor.service';
import { NiceAdminComponent } from './nice-admin/nice-admin.component';
import { SidebaritemComponent, SidebaritemInnerComponent } from './nice-admin/sidebaritem.component';
import { DayCloseChequeComponent } from './day-close-cheque/day-close-cheque.component';
import { TabsModule } from 'ngx-bootstrap/tabs';
import { CollapseModule } from 'ngx-bootstrap/collapse';
import { FrontEndPasswordChangeComponent } from './front-end-password-change/front-end-password-change.component';
import { ForgetPassComponent } from './login/forget-pass.component';

import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { SettingsComponent } from './settings/settings.component';
import { AllUsersComponent } from './all-users/all-users.component';
import { FileUploadComponent } from './file-upload/file-upload.component';
import { RevenueReportComponent } from './revenue-report/revenue-report.component';
import { VerifyVoucherPortalComponent } from './verify-voucher-portal/verify-voucher-portal.component';
import { ChequeClearComponent } from './cheque-clear/cheque-clear.component';
import { VoucherCancelComponent } from './voucher-cancel/voucher-cancel.component';
import { VerifyDirectBankDepositComponent } from './verify-direct-deposit/verify-direct-deposit.component';
import { VerifyVoucherDirectDepositComponent } from './verify-voucher-direct-deposit/verify-voucher-direct-deposit.component';
import { ApproveDirectVoucherComponent } from './approve-direct-voucher/approve-direct-voucher.component';

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    NavbarComponent,
    MainBodyComponent,
    LoginComponent,
    VoucherBankComponent,
    BankComponent,
    BranchComponent,
    UsersComponent,
    VerifyVoucherComponent,
    BankUsersComponent,
    UserLimitComponent,
    ChangePasswordComponent,
    UserProfileComponent,
    ApproveVoucherComponent,
    ReportComponent,
    TrialComponent,
    TwoFaModalComponent,
    VoucherBankOffComponent,
    NewNavComponent,
    ChequeEntryComponent,
    EditVoucherComponent,
    VoucherTransferComponent,
    DayCloseComponent,
    ChequeReportComponent,
    NewDesignComponent,
    NewHeaderComponent,
    NewSidebarComponent,
    NewMainBodyComponent,
    NiceAdminComponent,
    SidebaritemComponent,
    SidebaritemInnerComponent,
    FrontEndPasswordChangeComponent,
    DayCloseChequeComponent,
    ForgetPassComponent,
    SettingsComponent,
    AllUsersComponent,
    FileUploadComponent,
    RevenueReportComponent,
    VerifyVoucherPortalComponent,
    ChequeClearComponent,
    VoucherCancelComponent,
    VerifyDirectBankDepositComponent,
    VerifyVoucherDirectDepositComponent,
    ApproveDirectVoucherComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    ToastrModule.forRoot(),
    NgSelectModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    PaginationModule.forRoot(),
    ModalModule.forRoot(),
    BsDropdownModule.forRoot(),
    TooltipModule.forRoot(),
    BsDatepickerModule.forRoot(),
    BsDropdownModule.forRoot(),
    CollapseModule.forRoot(),
    TranslateModule.forRoot({
      // defaultLanguage: 'np-NP',
      loader: {
        provide: TranslateLoader,
        useFactory: HttpLoaderFactory,
        deps: [HttpClient]
      }
    }),
  ],
  providers: [
    AppConfig,
    { provide: APP_INITIALIZER, useFactory: (config: AppConfig) => () => config.loadConfig(), deps: [AppConfig], multi: true },
	  { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
    {
      provide: LocationStrategy,
      useClass: HashLocationStrategy
    },
    {
        provide: HTTP_INTERCEPTORS,
        useClass: HttpInterceptorService,
        multi: true,
    },
    AuthGuard,
    AuthService,
    LoginGuard,
    ApiService,
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}

export function HttpLoaderFactory(http: HttpClient){
  return new TranslateHttpLoader(http, './assets/i18n/', '.json')
  // return new TranslateHttpLoader(http)

}
