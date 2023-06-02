import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ApiService } from '../api.service';
import { ToastrService } from 'ngx-toastr';
import { DatePipe } from '@angular/common';
import { ValidationService } from '../validation.service';
import { ActivatedRoute } from '@angular/router';
import { AppConfig } from '../app.config';
import { RevenueReportService } from './revenue-report.service';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'app-revneue-report',
  templateUrl: './revenue-report.component.html',
  styleUrls: ['./revenue-report.component.scss'],
  providers: [DatePipe]
})
export class RevenueReportComponent implements OnInit{

  formLayout: any;
  reportForm!: FormGroup;
  vs = ValidationService;

  formattedDateStartDate!: any;
  formattedDateEndDate!: any;

  model: any = {};

  tableView = false;
  dlgid : any;

  myDate: any = new Date();
  constructor(private fb: FormBuilder, private auth:AuthService, private toastr: ToastrService, private datePipe: DatePipe, private route: ActivatedRoute, private ap: AppConfig, private bvs: RevenueReportService) {
    
    const ud = this.auth.getUserDetails();
    if (ud) {
      this.dlgid = ud.dlgid;
    }
    
    this.myDate = this.datePipe.transform(this.myDate, 'yyyy-MM-dd');
    this.formLayout = {
      from:[this.myDate, Validators.required],
      to:[this.myDate, Validators.required],
      palika: [''],
      branches: [''],
      fy:[''],
      accno:[''],
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
    this.getusers();
    // this.getAccountNumbers();

   
  }

  lg : any

  userList:any;
  getusers(){
    this.bvs.getUserList().subscribe({
      next: (d) => {
        this.userList = d.data;
      }, error: err => {;
      }
    });
  }

  fiscalYear:any
  getFiscalYears(){
    this.bvs.getFy().subscribe({
      next: (d) => {
        this.fiscalYear = d.data;
        // console.log(d.data)
      }, error: err => {;
      }
    });
  }

  type:any;

  accreq : boolean | undefined
  makeAccountNumberRequired(){
    // const acc = this.reportForm.get('accno');
    // acc?.setValidators([Validators.required]);
    // acc?.updateValueAndValidity;
    this.accreq = true;
  }

  makeAccountNumberNotRequired(){
    // const acc = this.reportForm.get('accno');
    // acc?.clearValidators();
    // acc?.updateValueAndValidity;
    this.accreq = false;
  }

  reportType ="";

  parameterChange(){

      if (this.type == 'dbracdr'){
        this.reportType = "Default Branch revenue account collection Detailed Report";
        this.makeAccountNumberRequired();
      }
      else if (this.type == 'dbracr'){
        this.reportType = "Default Branch revenue account collection Report";
        this.makeAccountNumberNotRequired();
      }
      else if (this.type == 'obcr'){
        this.reportType = "Off branch Collection Report";
        this.makeAccountNumberNotRequired();
      }
      else if (this.type == 'obcrs'){
        this.reportType = "Off branch Collection Report Summary";
        this.makeAccountNumberNotRequired();
      }
      else if (this.type == 'dcr'){
        this.reportType = "Day Close Report";
        this.makeAccountNumberNotRequired();
      }
      else if (this.type == 'obcfob'){
        this.reportType = "Outside branch Collection for own branch";
        this.makeAccountNumberNotRequired();
      }
      else if (this.type == 'obcfobs'){
        this.reportType = "Outside branch Collection for own branch Summary";
        this.makeAccountNumberNotRequired();
      }
      else if (this.type == 'llrcr'){
        this.reportType = "Local Level Revenue Collection Report";
        this.makeAccountNumberNotRequired();
      }
  }


  url="taxpayer-voucher";
  cad=false;
  chd=false;
  vv=false;
  dc=false;
  sr=false;

  chkstatus!:boolean;

  setType(){
   var svalue = this.reportForm.value['type'];
   if(svalue=="cad"){
    this.chd = false;
    this.vv = false;
    this.cad = true;
    this.dc=false;
    this.chkstatus=false;
    this.sr=false;
   }
   else if(svalue=="chd"){
    this.cad = false;
    this.vv=false;
    this.dc=false
    this.chd = true;
    this.chkstatus=true;
   }
   else if(svalue=="vv"){
    this.cad = false;
    this.chd = false;
    this.dc=false
    this.vv=true;
    this.chkstatus=false;
    this.sr=false;
   }
   else if(svalue=="dc"){
    this.cad = false;
    this.chd = false;
    this.dc=true
    this.vv=false;
    this.chkstatus=false;
    this.sr=false;
   }
   else if(svalue=="sr"){
    this.cad = false;
    this.chd = false;
    this.dc=false;
    this.vv=false;
    this.sr=true;
    this.chkstatus=false;
   }
  }


  reportFormSubmit(){
    this.route.queryParams.subscribe(params => {
      this.type = params['type'];
    });
    if(this.reportForm.valid){
      const formElement = document.createElement('form');
      formElement.id = "repform"
      formElement.method = 'POST';
      formElement.action = this.ap.baseUrl + 'taxpayer-voucher/get-report-default-branch'; // replace with your actual form submission URL
      formElement.target = "_blank";
      Object.keys(this.reportForm.value).forEach(key => {
        var newField = document.createElement('input');
        newField.setAttribute('type', 'hidden');
        newField.setAttribute('name', key);
        newField.value = this.reportForm.value[key];
        formElement.appendChild(newField);
      });
      var newField = document.createElement('input');
      newField.setAttribute('type', 'hidden');
      newField.setAttribute('name', "_token");
      newField.value = this.auth.getUserDetails()?.token;
      formElement.appendChild(newField);

      var type = document.createElement('input');
      type.setAttribute('type', 'hidden');
      type.setAttribute('name', "type");
      type.value = this.type;
      formElement.appendChild(type);
      document.body.appendChild(formElement);

      if (this.type=="dbracdr"){
        if(this.reportForm.get('accno')?.value == null || this.reportForm.get('accno')?.value == ""){
          this.toastr.error("Account Number Cannot be empty");
          return;
        }
      }

      formElement.submit();
      setTimeout(()=>{
        document.getElementById('repform')?.remove();
      });
      //this.acs = undefined;
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
   this.sr=false;
   this.acs = undefined;
  }


  branches:any;
  getBranches() {
    this.branches = undefined;
    // this.reportForm.value['branches'];
      this.bvs.getBranches().subscribe({
        next: (d) => {
          this.branches = d.data;
        }, error: err => {
        }
      });

  }

  llgs:any;
  getllgs(){
    this.llgs = undefined;
    const llgs = this.reportForm.value['llgs'];
    this.bvs.getllgs().subscribe({
      next: (d) => {
        this.llgs = d.data;
        this.reportForm.patchValue({ "palika": this.dlgid });
        this.getAccountNumbers()
      }, error: err => {
      }
    });
  }

  acs: any;
  getAccountNumbers(){
    this.reportForm.patchValue({'accno':''});
    this.bvs.getAccountNumbers(this.reportForm.value.palika).subscribe({
      next: (d) => {
        this.acs = d.data;
      }, error: err => {
        // console.log(err);
      }
    });
  }


}
