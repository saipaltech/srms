import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '../auth/auth.service';
import { ValidationService } from '../validation.service';

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

    constructor(private router: Router,private AuthService: AuthService,
            private notify:ToastrService,private fb:FormBuilder ) {
              this.loginForm = this.fb.group({
                username:['',[Validators.required]],
                pincode:['',[Validators.required]],
                  password:['', Validators.required],
                  cpassword:['', Validators.required]
              });
    }
    ngOnInit() {

    }
    resetPassword() {
        if(this.loginForm.invalid){
            return ;
        }
        this.disabled = true;
        this.model = this.loginForm.value;
        this.AuthService.login(this.model.username, this.model.password)
          .subscribe({next:data=>{
          },error:err=>{
            this.notify.error(err.message);
                    this.disabled = false;
          }});
    }
}