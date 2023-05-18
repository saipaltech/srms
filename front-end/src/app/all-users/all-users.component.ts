import { Component, OnInit, Input, TemplateRef, ViewChild, ElementRef, OnDestroy } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { AllUsersService } from './all-users.service'
import { ValidationService } from '../validation.service';
import { BranchService } from '../branch/branch.service';


import { BsModalService, BsModalRef } from 'ngx-bootstrap/modal';
import { AuthService } from '../auth/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-all-users',
  templateUrl: './all-users.component.html',
})
export class AllUsersComponent implements OnInit, OnDestroy {

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
  branches: any;

  modalRef?: BsModalRef;

  resetPawsswordForm!: FormGroup;
  resetPawsswordFormLayout: any;
  userTypes:any;

  constructor(private auth:AuthService ,private toastr: ToastrService, private RS: AllUsersService,private router:Router) {
    this.srchForm = new FormGroup({
      entries: new FormControl('10'),
      srch_term: new FormControl('')
    })
  }
  ngOnDestroy(): void {
  //  console.log(this.toastr.currentlyActive);
  //   this.toastr.clear(this.toastr.currentlyActive);
  }

  loginUser(username:string){
    this.auth.loginUser(username).subscribe({next:(d)=>{
      this.router.navigate(["/"]).then(() => {
        window.location.reload();
      });
    },error:err=>{
      this.toastr.error(err.message);
    }});
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
      },
      error => {
        this.toastr.error(error.error);
      }
    );
  }

  isbtn = true


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
}
