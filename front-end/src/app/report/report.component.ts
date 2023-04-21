import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ApiService } from '../api.service';
import { ToastrService } from 'ngx-toastr';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-report',
  templateUrl: './report.component.html',
  styleUrls: ['./report.component.scss'],
  providers: [DatePipe]
})
export class ReportComponent {

  formLayout: any;

  reportForm!: FormGroup

  formattedDateStartDate!: any;
  formattedDateEndDate!: any;

  model: any = {};

  tableView = false;

  myDate: any = new Date();

  constructor(private fb: FormBuilder,private http: ApiService, private toastr: ToastrService, private datePipe: DatePipe,) {
    this.myDate = this.datePipe.transform(this.myDate, 'yyyy-MM-dd');

    this.formLayout = {
      from:[this.myDate, Validators.required],
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
        // console.log(this.model);
        this.tableView = true
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
   this.tableView = false;
  }

}
