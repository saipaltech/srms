import { Component, OnInit, TemplateRef } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { DatePipe } from '@angular/common';
import { FormBuilder, FormGroup, Validators, FormControl } from '@angular/forms';
import { VoucherService } from './voucher.service';
import { ValidationService } from '../validation.service';

import { BsModalService, BsModalRef } from 'ngx-bootstrap/modal';


@Component({
  selector: 'app-voucher-bank',
  templateUrl: './voucher-bank.component.html',
  styleUrls: ['./voucher-bank.component.scss'],
  providers: [DatePipe]
})
export class VoucherBankComponent implements OnInit {

  modalRef?: BsModalRef;


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

  approved ="";

constructor(private datePipe: DatePipe, private toastr: ToastrService, private fb: FormBuilder,private bvs:VoucherService, private modalService: BsModalService){
    this.myDate = this.datePipe.transform(this.myDate, 'yyyy-MM-dd');
    this.formLayout = {
      id:[],
      date: [this.myDate],
      voucherno: ['',Validators.required],
      taxpayername: ['',Validators.required],
      taxpayerpan: [''],
      depositedby:['',Validators.required],
      depcontact: ['',Validators.required],
      lgid: ['',Validators.required],
      // llgname: ['',Validators.required],
      collectioncenterid: ['',Validators.required],
      accountno:['',Validators.required],
      revenuecode: ['',Validators.required],
      purpose: [''],
      amount:['',Validators.required]
    }
    
    this.voucherBankForm =fb.group(this.formLayout)

    this.srchForm = new FormGroup({
      entries: new FormControl('10'),
      srch_term: new FormControl('')})
}

openModal(template: TemplateRef<any>) {
  this.modalRef = this.modalService.show(template);
}

changeFields() {
  var frm = document.getElementsByClassName('needs-validation')[0]
  var table = document.getElementsByClassName('tab')[0]

  var fd = document.getElementsByClassName('formdiv')[0]
  var td = document.getElementsByClassName('listdiv')[0]

  frm.classList.toggle('hide');
  table.classList.toggle('hide');
  fd.classList.toggle('hide');
  td.classList.toggle('hide');


  // this.toastr.success('Hello world!', 'Toastr fun!');
}

ngOnInit(): void {
  this.getList();
  
  this.bvs.getLocalLevels().subscribe({next:(dt)=>{
      this.llgs = dt.data;
    },error:err=>{

    }});

    this.bvs.getRevenue().subscribe({next:(dt)=>{
      this.revs = dt.data;
    },error:err=>{

    }});
}

getPalikaDetails(){
  const llgCode = this.voucherBankForm.value['lgid'];
  this.bvs.getPlaikaDetails(llgCode).subscribe({
    next:(d)=>{
      this.acs = d.bankacs;
      this.ccs = d.costcentres;
    },error:err=>{
      console.log(err);
    }
  });
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
  console.log(this.voucherBankForm.errors)
  if (this.voucherBankForm.valid) {
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
      this.voucherBankForm = this.fb.group(this.formLayout)
      this.getList();
    }, error:err => {
      this.toastr.error(err.error, 'Error');
    }
    });
  }

}

}

