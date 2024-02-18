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
import { timeout } from 'rxjs';

@Component({
  selector: 'app-report',
  templateUrl: './report.component.html',
  styleUrls: ['./report.component.scss'],
  providers: [DatePipe]
})
export class ReportComponent implements OnInit {

  formLayout: any;
  reportForm!: FormGroup;
  vs = ValidationService;

  formattedDateStartDate!: any;
  formattedDateEndDate!: any;

  model: any = {};

  tableView = false;

  myDate: any = new Date();
  constructor(private fb: FormBuilder, private auth: AuthService, private toastr: ToastrService, private datePipe: DatePipe, private route: ActivatedRoute, private ap: AppConfig, private bvs: ReportService) {
    this.myDate = this.datePipe.transform(this.myDate, 'yyyy-MM-dd');
    this.formLayout = {
      from: [this.myDate, Validators.required],
      to: [this.myDate, Validators.required],
      palika: [''],
      branches: [''],
      fy: [''],
      accno: [''],
      chkstatus: [''],
      users: [''],
      reporttype:['1']
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

  userList: any;
  getusers() {
    this.bvs.getUserList().subscribe({
      next: (d) => {
        this.userList = d.data;
      }, error: err => {
        ;
      }
    });
  }

  fiscalYear: any
  getFiscalYears() {
    this.bvs.getFy().subscribe({
      next: (d) => {
        this.fiscalYear = d.data;
        // console.log(d.data)
      }, error: err => {
        ;
      }
    });
  }

  type: any;

  reportType = "";

  parameterChange() {

    if (this.type == 'vv') {
      this.reportType = "Verified Voucher ";
      this.chkstatus = false;
    }
    else if (this.type == 'cad') {
      this.reportType = "Cash Deposit ";
      this.chkstatus = false;
    }
    else if (this.type == 'chd') {
      this.reportType = "Cheque Deposit ";
      this.chkstatus = true;
    }
    else if (this.type == 'dc') {
      this.reportType = "Day Close ";
      this.chkstatus = false;
    }
    else if (this.type == 'sr') {
      this.reportType = "Total Cash Collection ";
      this.chkstatus = false;

    }
    else if (this.type == 'bsr') {
      this.reportType = "Branch wise Collection ";
      this.chkstatus = false;

    }
  }


  url = "taxpayer-voucher";
  cad = false;
  chd = false;
  vv = false;
  dc = false;
  sr = false;
  bsr = false;

  chkstatus!: boolean;

  setType() {
    var svalue = this.reportForm.value['type'];
    if (svalue == "cad") {
      this.chd = false;
      this.vv = false;
      this.cad = true;
      this.dc = false;
      this.chkstatus = false;
      this.sr = false;
      this.bsr = false;
    }
    else if (svalue == "chd") {
      this.cad = false;
      this.vv = false;
      this.dc = false
      this.chd = true;
      this.bsr = false;
      this.chkstatus = true;
    }
    else if (svalue == "vv") {
      this.cad = false;
      this.chd = false;
      this.dc = false
      this.vv = true;
      this.chkstatus = false;
      this.sr = false;
      this.bsr = false;
    }
    else if (svalue == "dc") {
      this.cad = false;
      this.chd = false;
      this.dc = true
      this.vv = false;
      this.chkstatus = false;
      this.sr = false;
      this.bsr = false;
    }
    else if (svalue == "sr") {
      this.cad = false;
      this.chd = false;
      this.dc = false;
      this.vv = false;
      this.sr = true;
      this.bsr = false;
      this.chkstatus = false;
    }
    else if (svalue == "bsr") {
      this.cad = false;
      this.chd = false;
      this.dc = false;
      this.vv = false;
      this.sr = false;
      this.bsr = true;
      this.chkstatus = false;
    }
  }


  reportFormSubmit() {
    this.route.queryParams.subscribe(params => {
      this.type = params['type'];
    });
    if (this.reportForm.valid) {
      const formElement = document.createElement('form');
      formElement.id = "repform"
      formElement.method = 'POST';
      formElement.action = this.ap.baseUrl + 'taxpayer-voucher/get-report'; // replace with your actual form submission URL
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
      formElement.submit();
      setTimeout(()=>{
        document.getElementById('repform')?.remove();
      });
      this.acs = undefined;
    }
    else {
      Object.keys(this.reportForm.controls).forEach(field => {
        const singleFormControl = this.reportForm.get(field);
        singleFormControl?.markAsTouched({ onlySelf: true });
      });
      this.toastr.error('Unable to Fetch Data', 'Error');
    }
  }

  clearButton() {
    this.reportForm = this.fb.group(this.formLayout);
    this.model = undefined;
    this.cad = false;
    this.chd = false;
    this.dc = false;
    this.vv = false;
    this.sr = false;
    this.acs = undefined;
  }


  branches: any;
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

  llgs: any;
  getllgs() {
    this.llgs = undefined;
    // const llgs = this.reportForm.value['llgs'];
    this.bvs.getllgs().subscribe({
      next: (d) => {
        this.llgs = d.data;
        this.getAccountNumbers()
      }, error: err => {
      }
    });
  }

  acs: any;
  getAccountNumbers() {
    this.reportForm.patchValue({ 'accno': '' });
    this.bvs.getAccountNumbers(this.reportForm.value.palika).subscribe({
      next: (d) => {
        this.acs = d.data;
      }, error: err => {
        // console.log(err);
      }
    });
  }


}
