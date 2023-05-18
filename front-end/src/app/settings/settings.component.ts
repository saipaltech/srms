import { Component, OnInit } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { ApiService } from '../api.service';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent implements OnInit{
  otpSetupForm: any;
  formLayout:any;
  constructor(private http:ApiService,private toastr: ToastrService, private fb: FormBuilder){  
      this.formLayout = {
        otpmedium:['1'],
        supccu:['1']
      }
    this.otpSetupForm =fb.group(this.formLayout);
  }
  ngOnInit(): void {
    let pvalue:any={};
    this.http.get("settings/values").subscribe({next:(dt)=>{
      const data = dt.data;
      if(data.length){
        for(let it of data){
          pvalue[it.skey] = it.svalue;
        }
        this.otpSetupForm.patchValue(pvalue);
      }
    },error:err=>{

    }});
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
