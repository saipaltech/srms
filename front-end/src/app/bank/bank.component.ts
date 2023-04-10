import { Component, Input, TemplateRef } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import {BankService} from './bank.service';
import { ValidationService } from '../validation.service';

import { BsModalService, BsModalRef } from 'ngx-bootstrap/modal';

import {UsersService} from '../users/users.service'


@Component({
  selector: 'app-bank',
  templateUrl: './bank.component.html',
  styleUrls: ['./bank.component.scss']
})
export class BankComponent {

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
  banks:any;

  constructor(private toastr: ToastrService, private fb: FormBuilder, private RS: BankService){
    this.formLayout = {
      id:[],
      bankid: ['',Validators.required],
      disabled: ['0',Validators.required],
      approved: ['1',Validators.required],
      
    }
    this.bankForm =fb.group(this.formLayout)
    
    this.srchForm = new FormGroup({
      entries: new FormControl('10'),
      srch_term: new FormControl('')})
  }

  ngOnInit(): void {
    this.pagination.perPage = this.perPages[0];
    this.getList();
    this.getBanks();
  }
  getBanks(){
    this.RS.getlist().subscribe({next:(d)=>{
      this.banks = d;
      // console.log(this.banks);
    },error:err=>{

    }})
  }
  setName(index:any){
    if(index){
      this.bankForm.patchValue({"name":this.banks[index].name,"code":this.banks[index].code});
    }else{
      this.bankForm.patchValue({"name":'',"code":''});
    }
    
  }
  getList(pageno?: number | undefined) {
    const page = pageno || 1;
    this.RS.getList(this.pagination.perPage, page, this.searchTerm, this.column, this.isDesc).subscribe(
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

bankFormSubmit(){
  if (this.bankForm.valid) {
    this.model = this.bankForm.value;
    this.createItem(this.bankForm.value.id);
  } else {
    Object.keys(this.bankForm.controls).forEach(field => {
      const singleFormControl = this.bankForm.get(field);
      singleFormControl?.markAsTouched({onlySelf: true});
    });
    // this.toastr.error('Please fill all the required* fields', 'Error');
  }
}

isbtn = true

changeFields() {
  var frm = document.getElementsByClassName('needs-validation')[0]
  var table = document.getElementsByClassName('tab')[0]

  frm.classList.toggle('hide');
  table.classList.toggle('hide')


  this.isbtn = !this.isbtn;
}

resetForm(){
  this.bankForm =this.fb.group(this.formLayout);
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
      this.bankForm.patchValue({"bankid":id});
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



@Component({
  selector: 'app-bank-users',
  templateUrl: './bank-users.component.html'
})
export class BankUsersComponent {

  modalRef?: BsModalRef;

  bankUserForm:any;
  formLayout: any;
  vs = ValidationService;

  @Input() bankid!: string;
  
  constructor(private modalService: BsModalService, private fb: FormBuilder, private RS:UsersService, private toastr: ToastrService) {
    this.formLayout = {
      bankid:[''],
      username: ['',Validators.required],
      name: ['',Validators.required],
      post: ['',Validators.required],
      password: ['',Validators.required],
      mobile : ['', Validators.required]
      
    }
    this.bankUserForm =fb.group(this.formLayout)
  }
 
  openModal(template: TemplateRef<any>) {
    this.modalRef = this.modalService.show(template);
  }
  
  model: any = {};

  bankUserFormSubmit(){

    if (this.bankUserForm.valid) {
      this.model = this.bankUserForm.value;
      this.model.bankid=this.bankid;
      this.createItem(this.bankUserForm.value.id);
    } else {
      Object.keys(this.bankUserForm.controls).forEach(field => {
        const singleFormControl = this.bankUserForm.get(field);
        singleFormControl?.markAsTouched({onlySelf: true});
      });
      // this.toastr.error('Please fill all the required* fields', 'Error');
    }

  }

  resetForm(){
  this.bankUserForm =this.fb.group(this.formLayout);
}

createItem(id = null) {

  let upd = this.model;
  if (id != "" && id != null) {

    this.RS.update(id, upd).subscribe({
      next: (result :any) => {
      this.toastr.success('Item Successfully Updated!', 'Success');
      this.bankUserForm = this.fb.group(this.formLayout)
      // this.getList();
    }, error :err=> {
      this.bankUserForm.error(err.error.message, 'Error');
    }
    });
  } else {
    this.RS.createUser(upd).subscribe({
      next:(result:any) => {
      this.toastr.success('Item Successfully Saved!', 'Success');
      this.bankUserForm = this.fb.group(this.formLayout)
      // this.getList();
    }, error:err => {
      this.toastr.error(err.error.message, 'Error');
    }
    });
  }

}
}
