import { Component } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import {VerifyVoucherService} from './verify-voucher.service';
import { ValidationService } from '../validation.service';
import { DatePipe } from '@angular/common';


@Component({
  selector: 'app-verify-voucher',
  templateUrl: './verify-voucher.component.html',
  styleUrls: ['./verify-voucher.component.scss'],
  providers: [DatePipe]
})
export class VerifyVoucherComponent {

  vs = ValidationService;
  model: any = {};
  disabled = false;
  error = '';
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
  
  bankForm!: FormGroup;
  formLayout: any;
  myDate: any = new Date();

  constructor(private toastr: ToastrService, private fb: FormBuilder, private RS: VerifyVoucherService,private datePipe: DatePipe){
    this.myDate = this.datePipe.transform(this.myDate, 'yyyy-MM-dd');
    this.formLayout = {
      id:[],
      depositdate: [this.myDate],
      bankvoucherno: ['',Validators.required],
      remarks: ['',Validators.required],
      transactionid: ['',Validators.required],
      
    }
    this.bankForm =fb.group(this.formLayout)
    
    this.srchForm = new FormGroup({
      entries: new FormControl('10'),
      srch_term: new FormControl('')})
  }

  ngOnInit(): void {
    this.pagination.perPage = this.perPages[0];
    this.getList();
  }

  getList(pageno?: number | undefined) {
    const page = pageno || 1;
    this.RS.getList(this.pagination.perPage, page, this.searchTerm, this.column, this.isDesc).subscribe(
      (result: any) => {
        this.lists = result.data;
        this.pagination.total = result.total;
        this.pagination.currentPage = result.currentPage;
        console.log(result);
      },
      error => {
         this.toastr.error(error.error);
      }
    );
  }

bankFormSubmit(){
  // this.model.transactionid = this.transDetails.transactionid;
  // console.log (this.model.transactionid)

  this.bankForm.controls['transactionid'].setValue(this.transDetails.transactionid)

  if (this.bankForm.valid) {
    this.model = this.bankForm.value;
    this.bankForm.controls['transactionid'].setValue(this.transDetails.transactionid)
    // this.bankForm.controls['id'].setValue(this.transDetails.id)
    this.bankForm.patchValue({"id": this.transDetails.id})
    this.createItem(this.bankForm.value.id);
  } else {
    Object.keys(this.bankForm.controls).forEach(field => {
      const singleFormControl = this.bankForm.get(field);
      singleFormControl?.markAsTouched({onlySelf: true});
    });
    // this.toastr.error('Please fill all the required* fields', 'Error');
  }
}

changeFields() {
  var frm = document.getElementsByClassName('needs-validation')[0]
  var table = document.getElementsByClassName('tab')[0]

  frm.classList.toggle('hide');
  table.classList.toggle('hide')


  // this.toastr.success('Hello world!', 'Toastr fun!');
}

resetForm(){
  this.bankForm =this.fb.group(this.formLayout);
}

transDetails:any;
search() {
  this.RS.getTranactionData(this.srchForm.value.srch_term).subscribe({next:(dt)=>{
    
    this.transDetails = dt.data;
  },error:error=>{
    // console.log(error);
    // alert(5)
    this.toastr.error("error.error.message");
  }});
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
    this.RS.update(id, upd).subscribe({
      next: (result :any) => {
      this.toastr.success('Item Successfully Updated!', 'Success');
      this.bankForm = this.fb.group(this.formLayout)
      this.getList();
    }, error :err=> {
      this.toastr.error(err.error.message, 'Error');
    }
    });
  } else {
    this.RS.create(upd).subscribe({
      next:(result:any) => {
      this.toastr.success('Item Successfully Saved!', 'Success');
      this.bankForm = this.fb.group(this.formLayout)
      this.getList();
    }, error:err => {
      this.toastr.error(err.error.message, 'Error');
    }
    });
  }

}

getUpdateItem(id: any) {
  this.RS.getEdit(id).subscribe(
    (result: any) => {
      this.model = result;
      this.bankForm.patchValue(result);
      this.changeFields();
    },
    (error: any) => {
      this.toastr.error(error.error, 'Error');
    }
  );
}

deleteItem(id: any) {
  if (window.confirm('Are sure you want to delete this item?')) {
    this.RS.remove(id).subscribe((result: any) => {
      this.toastr.success('Item Successfully Deleted!', 'Success');
      this.getList();
    }, (error: { error: any; }) => {
      this.toastr.error(error.error, 'Error');
    });
  }
}


}
