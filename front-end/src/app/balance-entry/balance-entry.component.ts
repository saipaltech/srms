import { Component, OnInit } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { ApiService } from '../api.service';
import { VoucherService } from '../voucher-bank/voucher.service';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'app-balance-entry',
  templateUrl: './balance-entry.component.html',
  styleUrls: ['./balance-entry.component.scss']
})
export class BalanceEntryComponent implements OnInit{
  otpSetupForm: any;
  formLayout:any;
  dlgid:any;
  dataLoaded = false;
  constructor(private http:ApiService,private toastr: ToastrService, private fb: FormBuilder,private bvs: VoucherService,private auth: AuthService){  
    const ud = this.auth.getUserDetails();
    console.log(ud);
    if (ud) {
      this.dlgid = ud.dlgid;
    }
      this.formLayout = {
        closing:[''],
        opening:['']
      }
    // this.otpSetupForm =fb.group(this.formLayout);
  }
  branchdata:any;
  dlgname:any;
  ngOnInit(): void {
    
    this.otpSetupForm = this.fb.group({});
    this.http.get("taxpayer-voucher/balanceconfig?dlgid="+this.dlgid).subscribe({next:(dt)=>{
      this.branchdata = dt.data;
      this.dlgname=this.branchdata[0].lgnamenp;
      this.initializeFormControls();
      this.dataLoaded = true;
      this.getupdateItem();
    
    },error:err=>{

    }});
    // this.otpSetupForm = this.fb.group({});
    // this.branchdata.forEach((item: { id: string; }) => {
    //   this.otpSetupForm.addControl('id' + item.id, this.fb.control(item.id));
    //   this.otpSetupForm.addControl('o' + item.id, this.fb.control(''));
    //   this.otpSetupForm.addControl('c' + item.id, this.fb.control(''));
    // });
  }
bdata:any;
  getupdateItem(){
    this.http.get("taxpayer-voucher/editbalance?dlgid="+this.dlgid).subscribe({next:(dt)=>{
      this.bdata = dt.data;
      this.patchValues();
      this.dataLoaded = true; 
    },error:err=>{

    }});
  }

  patchValues(){
    this.bdata.forEach((item: { bankorgid: any; opening: any; closing: any; }) => {
      // console.log(item.opening);
   
      this.otpSetupForm.patchValue({
        [`o${item.bankorgid}`]: item.opening,
        [`c${item.bankorgid}`]: item.closing
      });
    });
  }

  initializeFormControls(): void {
    this.branchdata.forEach((item: {
      lgid: any; bankorgid: any; 
}) => {
      this.otpSetupForm.addControl(`id${item.bankorgid}`, this.fb.control(item.bankorgid));
      this.otpSetupForm.addControl(`lgid${item.lgid}`, this.fb.control(item.lgid));
      this.otpSetupForm.addControl(`o${item.bankorgid}`, this.fb.control('0'));
      this.otpSetupForm.addControl(`c${item.bankorgid}`, this.fb.control('0'));
    });
  }
  model: any = {};
  
  otpSetupFormSubmit(){
    const formData = this.branchdata.map((item: { lgid: any; bankorgid: any; }) => ({
      
      bankorgid: item.bankorgid,
      lgid: item.lgid,
      opening: this.otpSetupForm.get(`o${item.bankorgid}`)?.value||0,
      closing: this.otpSetupForm.get(`c${item.bankorgid}`)?.value||0
    }));
    console.log(formData);
    // this.model=formData;
    this.model.balance=formData;
    this.model.lgid=this.dlgid;
    

    // console.log(this.otpSetupForm.value);

    this.http.post("taxpayer-voucher/balance-entry",this.model).subscribe({
      next:(dt)=>{
        this.getupdateItem();
        this.toastr.success(dt.message);
      },error:err=>{
        this.toastr.error(err.error.message);
      }
    });
  }
}
