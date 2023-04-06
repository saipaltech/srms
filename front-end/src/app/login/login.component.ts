import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '../auth/auth.service';
import { ValidationService } from '../validation.service';
import { BsModalRef, BsModalService, ModalOptions } from 'ngx-bootstrap/modal';

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

    constructor(private router: Router,private AuthService: AuthService,
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
        <input type="text" class="form-control form-control-sm" id="otp" formControlName="otp" [ngClass]="vs.getControlClass(otpForm.controls['otp'])">
        <div [ngClass]="vs.getMessageClass(otpForm.controls['otp'])">
            {{ vs.getMessage(otpForm.controls["otp"]) }}
        </div>
    </div>
    <button class="btn btn-success btn-sm" type="submit">Submit</button>
</form>
    </div>
  `
})
 
export class TwoFaModalComponent implements OnInit {
  reqid?: string;
  userid?: string;
  otpForm:FormGroup;
  vs=ValidationService;
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
        this.authService.loginWithOtp(data).subscribe({next:(dt)=>{
            this.bsModalRef.hide();
            this.notify.success("Login successful.");
            this.router.navigate(['/']);
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

  }
}
