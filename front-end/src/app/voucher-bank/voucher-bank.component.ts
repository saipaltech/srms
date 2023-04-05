import { Component, Input, OnInit, TemplateRef } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { DatePipe, DOCUMENT } from '@angular/common';
import { FormBuilder, FormGroup, Validators, FormControl } from '@angular/forms';
import { VoucherService } from './voucher.service';
import { ValidationService } from '../validation.service';

import { BsModalService, BsModalRef } from 'ngx-bootstrap/modal';

import { Router, NavigationExtras, Route } from '@angular/router';
import { AuthService } from '../auth/auth.service';



@Component({
  selector: 'app-voucher-bank',
  templateUrl: './voucher-bank.component.html',
  styleUrls: ['./voucher-bank.component.scss'],
  providers: [DatePipe]
})
export class VoucherBankComponent implements OnInit {

  modalRef?: BsModalRef;

  @Input() hideForm! : boolean;
  @Input() hideButton! : boolean;
  @Input() hideApproveButton! : boolean;


  myDate: any = new Date();
  vs = ValidationService;
  voucherBankForm!: FormGroup;
  formLayout: any;
  llgs:any;
  ccs:any;
  acs:any;
  revs:any;

  lists: any;
  perPages = [10, 20, 50, 100];
  pagination = {
    total: 0,
    currentPage: 0,
    perPage: 0
  };
  searchTerm: string = '';
  column: string = '';
  isDesc: boolean = false;
  srchForm!: FormGroup;
  model: any = {};
  dlgid:any;

  approved ="";

constructor(private datePipe: DatePipe, private toastr: ToastrService, private fb: FormBuilder,private bvs:VoucherService, private modalService: BsModalService, private r: Router,private auth:AuthService){
  const ud = this.auth.getUserDetails();
  console.log(ud);
  if(ud){
    this.dlgid = ud.dlgid;
  }
  this.myDate = this.datePipe.transform(this.myDate, 'yyyy-MM-dd');
    this.formLayout = {
      id:[],
      date: [this.myDate],
      voucherno: [''],
      taxpayername: ['',Validators.required],
      taxpayerpan: ['',Validators.pattern('[0-9]+')],
      depositedby:['',Validators.required],
      depcontact: ['',[Validators.required,Validators.pattern('[0-9]+')]],
      lgid: ['',Validators.required],
      // llgname: ['',Validators.required],
      collectioncenterid: ['',Validators.required],
      accountno:['',Validators.required],
      revenuecode: ['',Validators.required],
      purpose: [''],
      amount:['',[Validators.required,Validators.pattern('[0-9]+')]]
    }
    
    this.voucherBankForm =fb.group(this.formLayout)
    

    this.srchForm = new FormGroup({
      entries: new FormControl('10'),
      srch_term: new FormControl('')})
}

openModal(template: TemplateRef<any>, id:any) {
  this.modalRef = this.modalService.show(template);
  this.getDetails(id);
}

details : any;

getDetails(id:any){
  this.bvs.getDetails(id).subscribe({next:(dt)=>{
    this.details = dt;
  },error:err=>{
    this.toastr.error("Unable to Fetch Data","Error")
  }});
}

isbtn = true;

changeFields() {
  //this.r.navigate(['report'+ '/1'])
  // window.open("/#/trial?voucherno="+"33"+'&palika='+"100612250451902230", '_blank')
  // window.open("/#/trial?voucherno="+"33"+'&palika='+"100612250451902230")


  this.isbtn=!this.isbtn;
  this.hideForm = !this.hideForm;

  
}

ngOnInit(): void {
  this.pagination.perPage = this.perPages[0];
  this.getList();
  this.voucherBankForm.get("lgid")?.valueChanges.subscribe({next:(d)=>{
    this.getPalikaDetails();
  }});
  this.bvs.getLocalLevels().subscribe({next:(dt)=>{
      this.llgs = dt.data;
      this.voucherBankForm.patchValue({"lgid":this.dlgid});
    },error:err=>{

    }});

    this.bvs.getRevenue().subscribe({next:(dt)=>{
      this.revs = dt.data;
    },error:err=>{

    }});
}

getPalikaDetails(){
  this.voucherBankForm.patchValue({"collectioncenterid":''});
  const llgCode = this.voucherBankForm.value['lgid'];
  this.bvs.getCostCentres(llgCode).subscribe({
    next:(d)=>{
      this.ccs = d.data;
      if(d.data.length==1){
        this.voucherBankForm.patchValue({"collectioncenterid":d.data[0].code});
      }
    },error:err=>{
      console.log(err);
    }
  });
}

getBankAccounts(){
  this.voucherBankForm.patchValue({"accountno":''});
  const llgCode = this.voucherBankForm.value['lgid'];
  const revenuecode = this.voucherBankForm.value['revenuecode'];
  if(llgCode && revenuecode){
    this.bvs.getBankAccounts(llgCode,revenuecode).subscribe({
      next:(d)=>{
        this.acs = d.data;
        if(d.data.length==1){
          this.voucherBankForm.patchValue({"accountno":d.data[0].acno});
        }
      },error:err=>{
        console.log(err);
      }
    });
  }
  
}


  showSuccess() {
    // this.toastr.success('Hello world!', 'Toastr fun!');
  }
  

  resetForm(){
    this.voucherBankForm =this.fb.group(this.formLayout);
  }

  getList(pageno?: number | undefined) {
    const page = pageno || 1;
    const approve = this.approved; 
    //2 is just a random number to specify to show all data
    this.bvs.getList(this.pagination.perPage, page, this.searchTerm, this.column, this.isDesc, approve).subscribe(
      (result: any) => {
        this.lists = result.data;
        this.pagination.total = result.total;
        this.pagination.currentPage = result.currentPage;
        // console.log(result);
      },
      error => {
         this.toastr.error(error.error.message);
      }
    );
  }

  setStatus(val: any){
    this.approved = val;
    this.getList();
  
  }


   
  

voucherBankFormSubmit(){
  if (this.voucherBankForm.valid) {
    const llgCode = this.voucherBankForm.value['lgid'];
    if(llgCode!=this.dlgid){
      if(!confirm("Default Local Level and Selected are not same, Are you sure to proceed")){
        return;
      }
    }
    this.model = this.voucherBankForm.value;
    this.createItem(this.voucherBankForm.value.id);
    // alert('submit but not create')
    console.log(this.voucherBankForm.value)

  } else {
    // alert('Invalid form')
    // console.log(this.voucherBankForm.value)
    Object.keys(this.voucherBankForm.controls).forEach(field => {
      const singleFormControl = this.voucherBankForm.get(field);
      singleFormControl?.markAsTouched({onlySelf: true});
    });
    // this.toastr.error('Please fill all the required* fields', 'Error');
  }
}

search() {
  this.pagination.perPage=this.srchForm.value.entries;
  this.searchTerm=this.srchForm.value.srch_term;
  this.getList();
}

resetFilters() {
  this.isDesc = false;
  this.column = '';
  this.searchTerm = '';
  this.pagination.currentPage = 1;
  this.getList();
}

paginatedData($event: { page: number | undefined; }) {
  this.getList($event.page);
}

changePerPage(perPage: number) {
  this.pagination.perPage = perPage;
  this.pagination.currentPage = 1;
  this.getList();
}


createItem(id = null) {

  let upd = this.model;
  if (id != "" && id != null) {

    this.bvs.update(id, upd).subscribe({
      next: (result :any) => {
      this.toastr.success('Item Successfully Updated!', 'Success');
      this.voucherBankForm = this.fb.group(this.formLayout)
      this.getList();
    }, error :err=> {
      this.toastr.error(err.error, 'Error');
    }
    });
  } else {
    this.bvs.create(upd).subscribe({
      next:(result:any) => {
        // alert('create')
      this.toastr.success('Item Successfully Saved!', 'Success');
      // this.r.navigate(['report'], { state: { data: upd } });
      window.open("/#/report-generate?voucherno="+upd.voucherno+'&palika='+upd.lgid, '_blank')
      this.voucherBankForm = this.fb.group(this.formLayout);
      this.voucherBankForm.get("lgid")?.valueChanges.subscribe({next:(d)=>{
        this.getPalikaDetails();
      }});
      this.acs=null;
      this.voucherBankForm.patchValue({"lgid":this.dlgid});
      this.getList();
    }, error:err => {
      this.toastr.error(err.error, 'Error');
    }
    });
  }

}

}

