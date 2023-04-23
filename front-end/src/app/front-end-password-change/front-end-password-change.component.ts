import { Component } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { BsModalRef} from 'ngx-bootstrap/modal';
import { ToastrService } from 'ngx-toastr';
import { ValidationService } from '../validation.service';
import { Location } from '@angular/common';
import { UserProfileService } from '../user-profile/user-profile.service';

@Component({
  selector: 'app-front-end-password-change',
  templateUrl: './front-end-password-change.component.html',
  styleUrls: ['./front-end-password-change.component.scss']
})
export class FrontEndPasswordChangeComponent {
  loginForm : FormGroup;
  model: any = {};
  disabled = false;
  error = '';
  vs = ValidationService;
  info:any={};
  bsModalRef?: BsModalRef;
  username="";
  constructor(private router: Router,private l: Location,
          private notify:ToastrService,private fb:FormBuilder, private RS: UserProfileService) {
      this.loginForm = this.fb.group({
        oldpassword:['',[Validators.required]],
          password:['', Validators.required],
          cpassword:['', Validators.required]
      });


  }
  ngOnInit() {
    const st:any = this.l.getState();
    this.username = st.username;
    // console.log(this.username);
  }
  changePassword() {
      if(this.loginForm.invalid){
          return ;
      }
      this.disabled = true;
      this.model = this.loginForm.value;
      this.model.username=this.username;
      this.RS.changePasswordLogin(this.model)
        .subscribe({next:data=>{
          // this.openModalWithComponent(data.reqid,data.userid);
              this.notify.success(data.message);
              this.router.navigate(['/login']);
        },error:err=>{
          this.notify.error(err.message);
                  this.disabled = false;
        }});
  }
}
