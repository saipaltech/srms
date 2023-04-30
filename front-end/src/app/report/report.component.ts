import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ApiService } from '../api.service';
import { ToastrService } from 'ngx-toastr';
import { DatePipe } from '@angular/common';
import { ValidationService } from '../validation.service';
import { ActivatedRoute } from '@angular/router';
import { AppConfig } from '../app.config';
import { ReportService } from './report.service';
import { AuthService } from '../auth/auth.service';

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
  token:any;
  constructor(private fb: FormBuilder,private http: ApiService, private auth:AuthService, private toastr: ToastrService, private datePipe: DatePipe, private route: ActivatedRoute, private ap: AppConfig, private bvs: ReportService) {
    this.myDate = this.datePipe.transform(this.myDate, 'yyyy-MM-dd');
   
    this.formLayout = {
      from:[this.myDate, Validators.required],
      to:[this.myDate, Validators.required],
      palika: [''],
      branches: [''],
      fy:['']
    }

    this.reportForm = fb.group(this.formLayout)
  }
  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.type = params['type'];
      this.parameterChange();
    });
    this.getFiscalYears();
    this.getllgs();
    this.getBranches();
  }
  
  fiscalYear:any
  getFiscalYears(){
    this.bvs.getFy().subscribe({
      next: (d) => {        
        this.fiscalYear = d.data;
        console.log(d.data)
      }, error: err => {;
      }
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


  reportFormSubmit(){

    console.log(this.reportForm.value.from);
    console.log(this.reportForm.value.to);

    if(this.reportForm.valid){
      this.model = this.reportForm;
      this.route.queryParams.subscribe(params => {
        this.type = params['type'];
      });
      // console.log(this.type)
      const details = this.auth.getUserDetails();
      if (details) {
        this.token = details.token;
      }
      window.open(this.ap.baseUrl+'taxpayer-voucher/get-report'+"?from="+this.reportForm.value.from+"&to="+this.reportForm.value.to+"&type="+this.type+"&palika="+this.reportForm.value.palika+"&branch="+this.reportForm.value.branches+"&fy="+this.reportForm.value.fy+"&_token="+this.token, "_blank");
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


  branches:any;
  getBranches() {
    this.branches = undefined;
    // this.reportForm.value['branches'];
      this.bvs.getBranches().subscribe({
        next: (d) => {
          this.branches = d.data;
          if (d.data.length == 1) {
            this.reportForm.patchValue({ "branches": d.data[0].id });
          }
        }, error: err => {
        }
      });

  }

  llgs:any;
  getllgs(){
    this.llgs = undefined;
    // const llgs = this.reportForm.value['llgs'];
    this.bvs.getllgs().subscribe({
      next: (d) => {        
        this.llgs = d.data;
        console.log(d.data[0].id)
        if (d.data.length == 1) {
          // console.log(d.data[0].id)
          this.reportForm.patchValue({ "palika": d.data[0].id });
        }
      }, error: err => {
      }
    });
  }


}
