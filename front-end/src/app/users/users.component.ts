import { Component, OnInit, Input, TemplateRef, ViewChild, ElementRef, OnDestroy } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { UsersService } from './users.service'
import { ValidationService } from '../validation.service';
import { BranchService } from '../branch/branch.service';


import { BsModalService, BsModalRef } from 'ngx-bootstrap/modal';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'app-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.scss']
})
export class UsersComponent implements OnInit, OnDestroy {
  UserImportForm: any;
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
  formLayout1: any;
  branches: any;

  modalRef?: BsModalRef;

  resetPawsswordForm!: FormGroup;
  resetPawsswordFormLayout: any;
  userTypes:any;
  baseUrl:any;

  constructor(private auth:AuthService,private toastr: ToastrService, private fb: FormBuilder, private RS: UsersService, private bs: BranchService, private modalService: BsModalService) {
    this.formLayout = {
      id: [],
      branchid: ['', Validators.required],
      name: ['', Validators.required],
      post: ['', Validators.required],
      username: ['', Validators.required],
      password: ['', [Validators.required, Validators.pattern('^(?=.*[0-9])(?=.*[!@#$%^&*])(?=.*[A-Z]).{8,}$')]],
      disabled: ['0', Validators.required],
      approved: ['1', [Validators.required]],
      mobile: ['', [Validators.required,Validators.pattern('^[0-9]{10}$')]],
      email:['',[Validators.required,Validators.email]],
      amountlimit: [''],
      permid: ['', Validators.required]

    }
    this.baseUrl = RS.baseurl;
    this.formLayout1 = {
      userImport:[''],
    }
  this.UserImportForm =fb.group(this.formLayout1);

    this.resetPawsswordFormLayout = {
      id: [],
      password: [],
      cpassword: []
    }

    this.resetPawsswordForm = fb.group(this.resetPawsswordFormLayout);
    this.bankForm = fb.group(this.formLayout);

    this.srchForm = new FormGroup({
      entries: new FormControl('10'),
      srch_term: new FormControl('')
    })
  }
  token(){
    return this.auth.getUserDetails()?.token;
  }
  ngOnDestroy(): void {
  //  console.log(this.toastr.currentlyActive);
  //   this.toastr.clear(this.toastr.currentlyActive);
  }

  resetPawsswordFormSubmit() {
    // alert('here')
    if (this.resetPawsswordForm.valid) {
      this.model = this.resetPawsswordForm.value;
      // this.resetPawsswordForm.patchValue({id, resetItem: this.createResetItem});
      this.createResetItem(this.currentUserId);
    } else {
      Object.keys(this.resetPawsswordForm.controls).forEach(field => {
        const singleFormControl = this.resetPawsswordForm.get(field);
        singleFormControl?.markAsTouched({ onlySelf: true });
      });
    }

  }

  UserImportFormSubmit(){
    const fileInput = document.getElementById('formFileSm') as HTMLInputElement;
  
  if (fileInput && fileInput.files && fileInput.files.length > 0) {
    const file: File = fileInput.files[0];
    const fileName: string = file.name;
    const fileExtension: string = fileName.substring(fileName.lastIndexOf('.') + 1);
    if(fileExtension=="xlsx"){
    let formData: FormData = new FormData();
    formData.append('file', file, file.name);
    this.RS.uploadFile(formData).subscribe({
      next: (data:any) => {
        this.toastr.success(data.message, 'Success');
      }, error: error => {
        this.toastr.error(error.error.message, 'Error');
        // console.log(error);
      }
    });
  }else{
    this.toastr.error("Invalid file format. Please upload xlsx file type.",'Error');
  }
    
  } else {
    this.toastr.error("No file Selected",'Error');
  }
  }

  createResetItem(id:any) {
    let upd = this.model;
    if (id != "" && id != null) {
      // this.resetPawsswordForm.setValue({id: this.createResetItem,});
      // alert('here')
      this.RS.resetPassword(id, upd).subscribe({
        next: (result: any) => {
          this.toastr.success(result.message, 'Success');
          this.resetPawsswordForm = this.fb.group(this.resetPawsswordFormLayout);  
          this.modalRef?.hide();   
        }, error: err => {
          this.toastr.error(err.error.message, 'Error');
        }
      });
    } else {
      this.toastr.error("ID Not Available", "Error")
    }

  }

  resetpwdForm() {
    this.resetPawsswordForm = this.fb.group(this.resetPawsswordFormLayout);
  }

  currentUserId: any;

  openModal(template: TemplateRef<any>, id: any) {
    this.modalRef = this.modalService.show(template); this.currentUserId = id;
  }


  ngOnInit(): void {
    this.pagination.perPage = this.perPages[0];
    this.getList();
    this.getBranches();
    this.getUserTypes();
    // this.toastr.info("Password must be at least eight characters long and must contain at least one uppercase,number and special character. Eg. Srms@123#","Password Information",{timeOut: 0})

  }
  getUserTypes(){
    this.RS.getUserTypes().subscribe({next:(d)=>{
      this.userTypes = d; 
    },error:(er)=>{
      console.log(er);
    }});
  }

  getBranches() {
    this.branches = undefined;
    // const br = this.bankForm.value['branchid'];
    this.bs.getlist().subscribe({
      next: (d: any) => {
        this.branches = d;
      }, error: err => {

      }
    });
  }

  getList(pageno?: number | undefined) {
    const page = pageno || 1;
    this.RS.getList(this.pagination.perPage, page, this.searchTerm, this.column, this.isDesc).subscribe(
      (result: any) => {
        this.lists = result.data;
        this.pagination.total = result.total;
        this.pagination.currentPage = result.currentPage;
      },
      error => {
        this.toastr.error(error.error);
      }
    );
  }

  bankFormSubmit() {
    if (this.bankForm.valid) {
      if (this.bankForm.value.permid == 3) {
        if ((this.bankForm.value.amountlimit+"").trim()=='') {
          this.toastr.warning("Set limit for the user" + this.bankForm.value.amountlimit, "Warning");
          return;
        }
      }
      this.model = this.bankForm.value;
      this.createItem(this.bankForm.value.id);
    } else {
      Object.keys(this.bankForm.controls).forEach(field => {
        const singleFormControl = this.bankForm.get(field);
        singleFormControl?.markAsTouched({ onlySelf: true });
      });
    }
  }

  isbtn = true


  changeFields() {
    var frm = document.getElementsByClassName('needs-validation')[0];
    var table = document.getElementsByClassName('tab')[0];
    frm.classList.toggle('hide');
    table.classList.toggle('hide');
    this.isbtn = !this.isbtn;
  }

  resetForm() {
    this.bankForm = this.fb.group(this.formLayout);
    this.enableAllForms();
  }


  search() {
    this.pagination.perPage = this.srchForm.value.entries;
    this.searchTerm = this.srchForm.value.srch_term;
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
        next: (result: any) => {
          this.toastr.success('Item Successfully Updated!', 'Success');
          this.bankForm = this.fb.group(this.formLayout);
          this.enableAllForms();
          this.getList();
        }, error: err => {
          this.toastr.error(err.error.message, 'Error');
        }
      });
    } else {
      this.RS.create(upd).subscribe({
        next: (result: any) => {
          this.toastr.success('Item Successfully Saved!', 'Success');
          this.bankForm = this.fb.group(this.formLayout)
          this.getList();
        }, error: err => {
          this.toastr.error(err.error.message, 'Error');
        }
      });
    }

  }


  @ViewChild('password') password!: any;
  fieldreq = true;
  pwdShowHide = true

  changeFieldsForEdit() {
    // this.isReadOnly = true;
    // this.disabledField = true;

    this.bankForm.get('username')?.disable();
    const pwd = this.bankForm.get('password');
    pwd?.disable();
    pwd?.clearValidators();
    pwd?.patchValue("");

    this.pwdShowHide = false;
    this.fieldreq = false;

  }

  enableAllForms() {
    this.bankForm.get('username')?.enable();
    this.bankForm.get('password')?.enable();
    this.fieldreq = true;
    this.pwdShowHide = true;

  }

  getUpdateItem(id: any) {
    this.RS.getEdit(id).subscribe(
      (result: any) => {
        this.model = result;
        this.bankForm.patchValue(result);
        this.changeFields();
        this.changeFieldsForEdit();
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
  selector: 'app-user-limit',
  templateUrl: './user-limit.component.html'
})
export class UserLimitComponent {
  @Input() userid?: string;
  modalRef?: BsModalRef;

  bankUserForm: any;
  formLayout: any;
  vs = ValidationService;



  constructor(private modalService: BsModalService, private fb: FormBuilder, private RS: UsersService, private toastr: ToastrService) {
    this.formLayout = {
      userid: [this.userid],
      limit: ['', Validators.required]

    }
    this.bankUserForm = fb.group(this.formLayout)
  }

  openModal(template: TemplateRef<any>) {
    this.modalRef = this.modalService.show(template);
  }

  resetForm() {
    this.bankUserForm = this.fb.group(this.formLayout);
  }

  bankUserFormSubmit() {

  }

}



@Component({
  selector: 'app-user-change-password',
  templateUrl: './change-password.component.html'
})
export class ChangePasswordComponent {
  @Input() userid?: string;
  modalRef?: BsModalRef;

  bankUserForm: any;
  formLayout: any;
  vs = ValidationService;



  constructor(private modalService: BsModalService, private fb: FormBuilder, private RS: UsersService, private toastr: ToastrService) {
    this.formLayout = {
      userid: [this.userid],
      password: ['', Validators.required],

    }
    this.bankUserForm = fb.group(this.formLayout)
  }

  openModal(template: TemplateRef<any>) {
    this.modalRef = this.modalService.show(template);
  }

  resetForm() {
    this.bankUserForm = this.fb.group(this.formLayout);
  }

  bankUserFormSubmit() {

  }

}
