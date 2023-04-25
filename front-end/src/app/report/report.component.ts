import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ApiService } from '../api.service';
import { ToastrService } from 'ngx-toastr';
import { DatePipe } from '@angular/common';
import { ValidationService } from '../validation.service';
import { ActivatedRoute } from '@angular/router';
import { AppConfig } from '../app.config';

@Component({
  selector: 'app-report',
  templateUrl: './report.component.html',
  styleUrls: ['./report.component.scss'],
  providers: [DatePipe]
})
export class ReportComponent implements OnInit{

  formLayout: any;

  reportForm!: FormGroup;
  vs = ValidationService;

  formattedDateStartDate!: any;
  formattedDateEndDate!: any;

  model: any = {};

  tableView = false;

  myDate: any = new Date();

  constructor(private fb: FormBuilder,private http: ApiService, private toastr: ToastrService, private datePipe: DatePipe, private route: ActivatedRoute, private ap: AppConfig) {
    this.myDate = this.datePipe.transform(this.myDate, 'yyyy-MM-dd');

    this.formLayout = {
      from:[this.myDate, Validators.required],
      to:[this.myDate, Validators.required],
    }

    this.reportForm = fb.group(this.formLayout)
  }
  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.type = params['type'];
    });
  }
  type:any;

  reportType ="";

  parameterChange(){

      if (this.type == 'vv'){
        this.reportType = "Verified Voucher "
      }
      else if (this.type == 'cad'){
        this.reportType = "Cash Deposit "
      }
      else if (this.type == 'chd'){
        this.reportType = "Cheque Deposit "
      }
      else if (this.type == 'dc'){
        this.reportType = "Day Close "
      }
  }


  url="taxpayer-voucher"; 
  cad=false;
  chd=false;
  vv=false;
  dc=false;


  setType(){
   var svalue = this.reportForm.value['type'];
   if(svalue=="cad"){    
    this.chd = false;
    this.vv = false; 
    this.cad = true;
    this.dc=false
   }
   else if(svalue=="chd"){
    this.cad = false;
    this.vv=false;
    this.dc=false
    this.chd = true;
   }
   else if(svalue=="vv"){
    this.cad = false;
    this.chd = false;
    this.dc=false
    this.vv=true;
   }
   else if(svalue=="dc"){
    this.cad = false;
    this.chd = false;
    this.dc=true
    this.vv=false;
   }
  }

  i=1;

  // this.route.queryParams.subscribe(params => {
  //   this.voucherno = params['voucherno'];
  //   this.palika = params['palika'];

  // });

  reportFormSubmit(){

    console.log(this.reportForm.value.from);
    console.log(this.reportForm.value.to);

    if(this.reportForm.valid){
      this.model = this.reportForm;
      this.route.queryParams.subscribe(params => {
        this.type = params['type'];
      });
      console.log(this.type)
      window.open(this.ap.baseUrl+'taxpayer-voucher/get-report'+"?from="+this.reportForm.value.from+"&to="+this.reportForm.value.to+"&type="+this.type, "_blank");
    //   this.http.get(this.url+'/get-report'+"?from="+this.reportForm.value.from+"&to="+this.reportForm.value.to+"&type='"+this.type+"'").subscribe({next: (data) =>{
    //     this.model = data;
    //     // console.log(this.model);
    //     // this.tableView = true
    //     this.setType();
    //   } 
    // })
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
   this.cad = false;
   this.chd = false;
   this.dc=false;
   this.vv=false;
  }

}
