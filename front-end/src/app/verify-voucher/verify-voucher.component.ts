import { Component } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { VerifyVoucherService } from './verify-voucher.service';
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
  srchFormList!: FormGroup;
  bankForm!: FormGroup;
  chequeForm!: FormGroup;
  formLayout: any;
  formLayout1: any;
  myDate: any = new Date();
  
  selectedval=new Array();

  constructor(private toastr: ToastrService, private fb: FormBuilder, private RS: VerifyVoucherService, private datePipe: DatePipe) {
    this.myDate = this.datePipe.transform(this.myDate, 'yyyy-MM-dd');
    this.formLayout = {
      id: [''],
      amount: ['', [Validators.required, Validators.pattern('[0-9]+')]],
      depositdate: [this.myDate],
      bankvoucherno: [Math.floor(10000000 + Math.random() * 90000000)],
      remarks: ['', Validators.required],
      transactionid: ['', Validators.required],

    }

    this.formLayout1 = {
      id: [''],
      transactionid: ['', Validators.required],
      options: this.fb.array([], [Validators.required])

    }
    this.bankForm = fb.group(this.formLayout);
    this.chequeForm = fb.group(this.formLayout1);

    this.srchForm = this.fb.group({
      entries: ['10'],
      srch_term: ['', [Validators.required]]
    })

    this.srchFormList = this.fb.group({
      entries: ['10'],
      srch_term: ['']
    })
  }






  ngOnInit(): void {
    this.pagination.perPage = this.perPages[0];
    this.getList();
  }

  searchList() {
    this.pagination.perPage = this.srchFormList.value.entries;
    this.searchTerm = this.srchFormList.value.srch_term;
    this.getList();
  }

  getList(pageno?: number | undefined) {
    const page = pageno || 1;
    this.RS.getList(this.pagination.perPage, page, this.searchTerm, this.column, this.isDesc).subscribe(
      (result: any) => {
        this.lists = result.data;
        //console.log(this.lists)
        this.pagination.total = result.total;
        this.pagination.currentPage = result.currentPage;
        //console.log(result);
      },
      error => {
        this.toastr.error(error.error);
      }
    );
  }

  chequeFormSubmit(){

  }

  bankFormSubmit() {
    // this.model.transactionid = this.transDetails.transactionid;
    // console.log (this.model.transactionid)

    this.bankForm.controls['transactionid'].setValue(this.transDetails.transactionid)

    if (this.bankForm.valid) {
      this.model = this.bankForm.value;
      this.bankForm.controls['transactionid'].setValue(this.transDetails.transactionid)
      // this.bankForm.controls['id'].setValue(this.transDetails.id)
      this.bankForm.patchValue({ "id": this.transDetails.id })
      this.createItem(this.bankForm.value.id);
    } else {
      Object.keys(this.bankForm.controls).forEach(field => {
        const singleFormControl = this.bankForm.get(field);
        singleFormControl?.markAsTouched({ onlySelf: true });
      });
      // this.toastr.error('Please fill all the required* fields', 'Error');
    }
  }
  showList = false;
  showForm = true;
  cDt=false;
  changeFields() {
    this.showList = !this.showList;
    this.showForm = !this.showForm;
    var fd = document.getElementsByClassName('formdiv')[0]
    var td = document.getElementsByClassName('listdiv')[0]

    fd.classList.toggle('hide');
    td.classList.toggle('hide');
  }

  resetForm() {
    this.bankForm = this.fb.group(this.formLayout);
    this.showForm = !this.showForm;
    this.transDetails = "";
  }

  transDetails: any;
  istab = 1;
  search() {
    this.cDt=true;
    this.transDetails=undefined;
    if (this.srchForm.valid) {
      this.RS.getTranactionData(this.srchForm.value.srch_term).subscribe({
        next: (dt) => {
          this.transDetails = dt.data;
          this.bankForm.patchValue({ "id": this.transDetails.id });
          this.bankForm.patchValue({ "transactionid": this.transDetails.transactionid })
          if (this.transDetails.trantype == 1) {
            this.istab = 1;
          } else {
            this.istab = 2;
          }
        }, error: error => {
          // console.log(error);
          // alert(5)
          this.toastr.error(error.error.message, "Error");
        }
      });
    }
  }

  onCheckboxChange(e:any) {

    const options: FormArray = this.chequeForm.get('options') as FormArray;

    if (e.target.checked) {

      options.push(new FormControl(e.target.value));

    } else {

       const index = options.controls.findIndex(x => x.value === e.target.value);

       options.removeAt(index);

    }
    console.log(options.value);
    this.selectedval=options.value;
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
          this.transDetails = undefined;
          this.toastr.success('Item Successfully Updated!', 'Success');
          this.bankForm = this.fb.group(this.formLayout)
          this.getList();
        }, error: err => {
          this.toastr.error(err.error.message, 'Error');
        }
      });
    } else {
      this.RS.create(upd).subscribe({
        next: (result: any) => {
          this.transDetails = undefined;
          this.toastr.success('Item Successfully Saved!', 'Success');
          this.bankForm = this.fb.group(this.formLayout);
          this.srchForm.patchValue({'srch_term':""});
          this.getList();
        }, error: err => {
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
