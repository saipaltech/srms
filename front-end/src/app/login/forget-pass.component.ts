import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '../auth/auth.service';
import { ValidationService } from '../validation.service';
import { AppConfig } from '../app.config';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-login',
  templateUrl: './forget-pass.component.html',
  styleUrls: ['./login.component.scss']
})
export class ForgetPassComponent {
  loginForm : FormGroup;
    model: any = {};
    disabled = false;
    error = '';
    vs = ValidationService;
    info:any={};
    pinenable=true;

    constructor(private router: Router,private AuthService: AuthService,public appConfig:AppConfig,
            private notify:ToastrService,private fb:FormBuilder,private http: HttpClient ) {
              this.loginForm = this.fb.group({
                username:['',[Validators.required]],
                pincode:['',[Validators.required]],
                password:['', Validators.required,Validators.pattern('^(?=.*[0-9])(?=.*[!@#$%^&*])(?=.*[A-Z]).{8,}$')],
                cpassword:['', Validators.required]
              });
    }
    ngOnInit() {

    }

    getPincode(){
      const username = this.loginForm.get("username")?.value;
      if(username){
        this.pinenable = false;
        setTimeout(()=>{
          this.pinenable = true;
        },20000);
        this.http.post(this.appConfig.baseUrl+"auth/get-pincode",{username:username}).subscribe({next:(data:any)=>{
          this.notify.success(data.message);
        },error:err=>{
          this.notify.error(err.error.message);
        }});
      }else{
        this.notify.info("Please provide your username");
      }
    }
    resetPassword() {
        if(this.loginForm.invalid){
            return ;
        }
      this.disabled = true;
      this.model = this.loginForm.value;
      this.http.post(this.appConfig.baseUrl+"users/reset-passbypin",this.model)
        .subscribe({next:(data:any)=>{
          // this.openModalWithComponent(data.reqid,data.userid);
              this.notify.success(data.message);
              this.router.navigate(['/login']);
        },error:err=>{
          this.notify.error(err.error.message);
                  this.disabled = false;
        }});
    }
}