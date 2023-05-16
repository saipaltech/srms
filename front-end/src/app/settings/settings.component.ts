import { Component } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { AppConfig } from '../app.config';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent {
  otpSetupForm: any;
  formLayout:any;
  constructor(private appconfig:AppConfig,private toastr: ToastrService, private fb: FormBuilder){  
      this.formLayout = {
        otp:[]
      }      
    this.otpSetupForm =fb.group(this.formLayout);
  }

  otpSetupFormSubmit(){
    //update settings here at users Controller and settings 
  }
}
