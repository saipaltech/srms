import { Component, TemplateRef } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { ValidationService } from '../validation.service';
import { DatePipe } from '@angular/common';
import { VoucherTransferService } from './voucher-transfer.service';
import { VoucherServiceOff } from '../voucher-bank-off/voucher.service';
import { BsModalService, BsModalRef } from 'ngx-bootstrap/modal';


@Component({
  selector: 'app-voucher-transfer',
  templateUrl: './voucher-transfer.component.html',
  styleUrls: ['./voucher-transfer.component.scss'],
  providers: [DatePipe]
})
export class VoucherTransferComponent {

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
  formLayout: any;
  myDate: any = new Date();
  items=new Array();
  revs:any;

  modalRef?: BsModalRef;

  constructor(private toastr: ToastrService,private modalService: BsModalService ,private fb: FormBuilder, private RS: VoucherTransferService,private bvs:VoucherServiceOff, private datePipe: DatePipe) {
    this.myDate = this.datePipe.transform(this.myDate, 'yyyy-MM-dd');
    this.formLayout = {
      id: [],
     
      remarks: ['', Validators.required],
      revenuecode: [''],
      lgid:[''],
      collectioncenterid:[''],
      bankorgid:['']



    }
    this.bankForm = fb.group(this.formLayout)

    this.srchForm = this.fb.group({
      entries: ['10'],
      srch_term: ['', [Validators.required]]
    })

    this.srchFormList = this.fb.group({
      entries: ['10'],
      srch_term: ['']
    })
  }

  openModal(template: TemplateRef<any>, id:any) {
    this.details = undefined;
    this.modalRef = this.modalService.show(template, Object.assign({}, { class: 'gray modal-lg' }));
    this.getDetails(id);  
  }

  details : any;

getDetails(id:any){
  this.RS.getDetails(id).subscribe({next:(dt)=>{
    this.details = dt;
    // console.log(this.details);
  },error:err=>{
    this.toastr.error("Unable to Fetch Data","Error")
  }});
}

updateChanges(id:string,type:string){
  const msg = type=='1'?"Settle Updates":"Cancel Updates";
  if(confirm("You are about to "+msg+".")){
    this.RS.updateChanges(id,type).subscribe({next:(d:any)=>{
      this.toastr.success(d.message,"Success");
      this.getList();
      this.modalRef?.hide();    
    },error:err=>{
      this.toastr.error(err.error.message,"Error");
    }});
  }  
}


  ngOnInit(): void {
    this.pagination.perPage = this.perPages[0];
    this.bankForm.get("lgid")?.valueChanges.subscribe({next:(d)=>{
      this.getPalikaDetails();
      this.getBankAccounts();
    }});
    this.bvs.getLocalLevels().subscribe({next:(dt)=>{
      this.llgs = dt.data;
      // this.voucherBankForm.patchValue({"lgid":this.dlgid});
    },error:err=>{

    }});
    this.getList();
  }

  getPalikaDetails(){
    this.bankForm.patchValue({"collectioncenterid":''});
    const llgCode = this.bankForm.value['lgid'];
    this.bvs.getCostCentres(llgCode).subscribe({
      next:(d)=>{
        this.ccs = d.data;
        if(d.data.length==1){
          this.bankForm.patchValue({"collectioncenterid":d.data[0].code});
        }
      },error:err=>{
        // console.log(err);
      }
    });
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

acs:any;
llgs:any;
ccs:any;
getRevenue(){
  const bankorgid=this.bankForm.value["bankorgid"];
   this.RS.getRevenue(bankorgid).subscribe({next:(dt)=>{
      this.revs = dt.data;
    },error:err=>{

    }});

}
  getBankAccounts(){
    // this.acs  = undefined;
    const llgCode = this.bankForm.value['lgid'];
    if(llgCode){
      this.RS.getBankAccounts(llgCode).subscribe({
        next:(d)=>{
          this.acs = d.data;
          if(d.data.length==1){
            this.bankForm.patchValue({"bankorgid":d.data[0].id});
          }
        },error:err=>{
          // console.log(err);
        }
      });
    }
    
  }

  selectedRevenue:any;
  altmsg(msg:any){
    if(msg=="Invalid Pattern."){
      return "Number only";
    }
    return msg;
  }
  
  mobile(msg:any){
    if(msg=="Invalid Pattern."){
      return "Need 10 digit mobile number";
    }
    return msg;
  }

  addItem(){
    //  console.log(this.rv);
    
     let rc=this.bankForm.value['revenuecode'];
     let amt=this.bankForm.value['amount'];
     if(amt && rc && this.bankForm.get('amount')?.valid){
      let val;
      for (const item of this.revs) {
       if (item.code === rc) {
          val=item.code+'['+item.name+']';
         // console.log(`Found key-value pair: ${item.key} : ${item.value}`);
         break;
       }
     }
      var newItem = {
       rc: rc,
       amt: amt,
       rv:val
     };
     
     // Add the new item to the items array
     this.items.push(newItem);
     this.calctotal();
     this.bankForm.patchValue({"revenuecode":''});
     this.bankForm.patchValue({"amount":''});
    
     }
     
    
    }
    totalAmt=0;
    calctotal(){
      this.totalAmt=0;
      for(const item of this.items){
        this.totalAmt+=parseInt(item.amt);
     
      }
    }
    
  
  removeItem(index:any) {
   
    this.items.splice(index, 1);
    this.calctotal();
  }
  
  

  bankFormSubmit() {
    // this.model.transactionid = this.transDetails.transactionid;
    // console.log (this.model.transactionid)
    this.bankForm.patchValue({ "id": this.transDetails.id })
    if (this.bankForm.valid) {
      this.model = this.bankForm.value;
     
      // this.bankForm.controls['id'].setValue(this.transDetails.id)
     
      this.RS.create(this.model).subscribe({
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
    if (this.srchForm.valid) {
      this.RS.getTranactionData(this.srchForm.value.srch_term).subscribe({
        next: (dt) => {
          this.transDetails = dt.data;
          // this.bankForm.patchValue({'lgid':this.transDetails.lgid})
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
