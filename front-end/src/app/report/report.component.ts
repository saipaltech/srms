import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ApiService } from '../api.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-report',
  templateUrl: './report.component.html',
  styleUrls: ['./report.component.scss']
})
export class ReportComponent {

  formLayout: any;

  reportForm!: FormGroup

  formattedDateStartDate!: any;
  formattedDateEndDate!: any;

  model: any = {};

  constructor(private fb: FormBuilder,private http: ApiService, private toastr: ToastrService) {
    this.formLayout = {
      from:['', Validators.required],
      to:['', Validators.required]
    }

    this.reportForm = fb.group(this.formLayout)
  }

  url="taxpayer-voucher"; 

  reportFormSubmit(){

    console.log(this.reportForm.value.from);
    console.log(this.reportForm.value.to);

    if(this.reportForm.valid){
      this.model = this.reportForm;
      this.http.get(this.url+'/get-report'+"?from="+this.reportForm.value.from+"&to="+this.reportForm.value.to).subscribe({next: (data) =>{
        this.model = data;
        console.log(this.model);
      } 
    })
    }
    else{
      Object.keys(this.reportForm.controls).forEach(field => {
        const singleFormControl = this.reportForm.get(field);
        singleFormControl?.markAsTouched({onlySelf: true});
      });
        this.toastr.error('Unable to Fetch Data', 'Error');

    }
  }

  clearButton(){
   this.reportForm = this.fb.group(this.formLayout);
   this.model =  undefined;
  }

}
