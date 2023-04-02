import { APP_INITIALIZER, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HeaderComponent } from './header/header.component';
import { NavbarComponent } from './navbar/navbar.component';
import { MainBodyComponent } from './main-body/main-body.component';
import { LoginComponent } from './login/login.component';
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
    ApproveVoucherComponent
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
    BsDropdownModule.forRoot()
  ],
  providers: [
    AppConfig,
    { provide: APP_INITIALIZER, useFactory: (config: AppConfig) => () => config.loadConfig(), deps: [AppConfig], multi: true },
	  { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
    {
      provide: LocationStrategy,
      useClass: HashLocationStrategy
    },
    AuthGuard,
    AuthService,
    LoginGuard,
    ApiService,
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
