import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '../auth/auth.service';
import { UserProfileService } from './user-profile.service';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.scss']
})
export class UserProfileComponent implements OnInit {

  formlayout: any;

  passwordForm: any;

  model: any = {};

  username ="";

  constructor(private fb: FormBuilder, private RS: UserProfileService, private auth: AuthService, private toastr: ToastrService){
    this.formlayout = 
      {
        oldpassword: ['', Validators.required],
        password: ['', Validators.required],
        confirmpassword: ['', Validators.required]
      }

      this.passwordForm =fb.group(this.formlayout);

      const details = auth.getUserDetails();
      if (details) {
        this.username = details.username;
      }
    
  }

        
      
  ngOnInit(): void{

    this.getUser(this.username);

  }

  userDetails : any;

  getUser(username: any){
    this.RS.getDetails(username).subscribe({next:(dt)=>{
      this.userDetails = dt;
      // console.log(this.userDetails)
    },error:error=>{
      this.toastr.error("error.error.message");
    }});
  }

  passwordFormSubmit(){
    if (this.passwordForm.valid){
      this.model = this.passwordForm.value;
      this.createItem(6); //add id here
    }
  }

  createItem(id: any){

  }

}
