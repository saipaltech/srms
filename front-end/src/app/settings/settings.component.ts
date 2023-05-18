import { Component } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { AppConfig } from '../app.config';
import { ApiService } from '../api.service';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent {
  otpSetupForm: any;
  formLayout:any;
  constructor(private http:ApiService,private toastr: ToastrService, private fb: FormBuilder){  
      this.formLayout = {
        otpValue:[]
      }      
    this.otpSetupForm =fb.group(this.formLayout);
  }

  otpSetupFormSubmit(){
    this.http.post("settings",this.otpSetupForm.value).subscribe({
      next:(dt)=>{
        this.toastr.success(dt.message);
      },error:err=>{
        this.toastr.error(err.error.message);
      }
    });
  }
}
