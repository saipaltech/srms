import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '../auth/auth.service';
import { ValidationService } from '../validation.service';
import { BsModalRef, BsModalService, ModalOptions } from 'ngx-bootstrap/modal';
import { AppConfig } from '../app.config';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  loginForm : FormGroup;
    model: any = {};
    disabled = false;
    error = '';
    vs = ValidationService;
    info:any={};
    bsModalRef?: BsModalRef;

    constructor(private router: Router,private AuthService: AuthService,public appConfig:AppConfig,
            private notify:ToastrService,private fb:FormBuilder,private modalService: BsModalService ) {
        this.loginForm = this.fb.group({
            username:['',[Validators.required]],
            password:['', Validators.required]
        });
    }
    ngOnInit() {
    }
    login() {
        if(this.loginForm.invalid){
            return ;
        }
        this.disabled = true;
        this.model = this.loginForm.value;
        this.AuthService.login(this.model.username, this.model.password)
          .subscribe({next:data=>{
            if(data.token){
              this.router.navigate(['/']);
              return;
            }
            this.openModalWithComponent(data.reqid,data.userid);
                //this.notify.success("Login successful.");
                //this.router.navigate(['/']);
          },error:err=>{
            this.notify.error(err.message);
                    this.disabled = false;
          }});
    }

    openModalWithComponent(reqid:string,userid:string) {
      const initialState: ModalOptions = {
        initialState: {
          reqid:reqid,
          userid:userid
        },
        backdrop:'static'
      }
      this.bsModalRef = this.modalService.show(TwoFaModalComponent, initialState);
    }
}

@Component({
  selector: 'modal-content',
  template: `
    <div class="modal-header">
      <h4 class="modal-title pull-left">2FA: Enhancing Security</h4>
      <button type="button" class="btn-close close pull-right" aria-label="Close" (click)="bsModalRef.hide()">
        <span aria-hidden="true" class="visually-hidden">&times;</span>
      </button>
    </div>
    <div class="modal-body">
    <form class="row g-3 needs-validation" [formGroup]="otpForm" (ngSubmit)="submitOtp()" novalidate>
	<div class="mb-4">
		<label for="otp" class="form-label">Enter the authentication code sent to your registered mobile</label>
        <input #otpInput autocomplete="off" type="text" class="form-control form-control-sm" id="otp" formControlName="otp" [ngClass]="vs.getControlClass(otpForm.controls['otp'])">
        <div [ngClass]="vs.getMessageClass(otpForm.controls['otp'])">
            {{ vs.getMessage(otpForm.controls["otp"]) }}
        </div>
    </div>
    <div class="row">
      <div class="col-12"><p *ngIf="display">Time Remaining: {{display}}</p></div>
    </div>
    <button class="btn btn-success btn-sm" type="submit">Submit</button>
</form>
    </div>
  `
})

export class TwoFaModalComponent implements OnInit {
  @ViewChild("otpInput") otpField?: ElementRef;
  reqid?: string;
  userid?: string;
  otpForm:FormGroup;
  vs=ValidationService;
  display:any;
  constructor(public bsModalRef: BsModalRef,private fb:FormBuilder,private authService:AuthService,private notify:ToastrService,private router:Router) {
      this.otpForm = this.fb.group({
                  otp:['',[Validators.required,Validators.pattern('[0-9]+')]]
          });
  }
  submitOtp(){
    if (this.otpForm.valid) {
      const otp = this.otpForm.value['otp'];
      if(this.reqid && this.userid && otp){
        const data = {
          otp:otp,
          reqid:this.reqid,
          userid:this.userid
        };
        this.authService.loginWithOtp(data).subscribe({next:(dt:any)=>{
            this.bsModalRef.hide();
            if(dt.token){
              this.notify.success("Login successful.");
              this.router.navigate(['/']);
            }else{
              this.notify.info("You need to change password before login.");
              this.router.navigate(['/password-change'],{ state: { username: dt.username } });
            }
        },error:err=>{
          this.notify.error(err.message);
        }})
      }
    } else {
      Object.keys(this.otpForm.controls).forEach(field => {
        const singleFormControl = this.otpForm.get(field);
        singleFormControl?.markAsTouched({onlySelf: true});
      });
    }
  }

  ngOnInit() {
    this.timer(2);
    setTimeout(()=>{
      this.otpField?.nativeElement.focus();
    });
  }
  timer(minute:number) {
    // let minute = 1;
    let seconds: number = minute * 60;
    let textSec: any = "0";
    let statSec: number = 60;

    const prefix = minute < 10 ? "0" : "";

    const timer = setInterval(() => {
      seconds--;
      if (statSec != 0) statSec--;
      else statSec = 59;

      if (statSec < 10) {
        textSec = "0" + statSec;
      } else textSec = statSec;

      this.display = `${prefix}${Math.floor(seconds / 60)}:${textSec}`;

      if (seconds == 0) {
        console.log("finished");
        clearInterval(timer);
      }
    }, 1000);
  }
}
