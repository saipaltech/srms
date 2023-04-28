import { Component } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { ValidationService } from '../validation.service';
import { DatePipe } from '@angular/common';
import { EditVoucherService } from './edit-voucher.service';
import { ChequeEntryService } from '../cheque-entry/cheque-entry.service';
import { VoucherTransferService } from '../voucher-transfer/voucher-transfer.service';


@Component({
  selector: 'app-edit-voucher',
  templateUrl: './edit-voucher.component.html',
  styleUrls: ['./edit-voucher.component.scss'],
  providers: [DatePipe]
})
export class EditVoucherComponent {

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

  constructor(private toastr: ToastrService, private fb: FormBuilder,private rs:VoucherTransferService,private bvs:ChequeEntryService, private RS: EditVoucherService, private datePipe: DatePipe) {
    this.myDate = this.datePipe.transform(this.myDate, 'yyyy-MM-dd');
    this.formLayout = {
      id: [],
      taxpayername: ['', [Validators.required]],
      taxpayerpan: [''],
      // remarks: ['', Validators.required],
      amount:['',Validators.pattern('[0-9]+')],
      revenuecode: [''],
      lgid:['',Validators.required],
      bankorgid:['',Validators.required],
      collectioncenterid:['',Validators.required]



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




llgs:any;

  ngOnInit(): void {
    this.pagination.perPage = this.perPages[0];
    this.bankForm.get("lgid")?.valueChanges.subscribe({next:(d)=>{
      this.getPalikaDetails();
      this.getBankAccounts();
      // this.getRevenue();
    }});
    this.RS.getAllLocalLevels().subscribe({next:(dt)=>{
      this.llgs = dt.data;
    },error:err=>{

    }});
    // this.getRevenue();
    // this.getList();
  }
ccs:any;
  getPalikaDetails(){
    this.bankForm.patchValue({"collectioncenterid":''});
    const llgCode = this.bankForm.value['lgid'];
    this.bvs.getCostCentres(llgCode).subscribe({
      next:(d)=>{
        this.ccs = d.data;
        setTimeout(()=>{
          if(this.transDetails){
            // this.bankForm.patchValue({"collectioncenterid":this.transDetails.collectioncenterid});
          }
        });
        
      },error:err=>{
        // console.log(err);
      }
    });
  }


  searchList() {
    this.pagination.perPage = this.srchFormList.value.entries;
    this.searchTerm = this.srchFormList.value.srch_term;
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
     console.log(this.items);
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

    // this.bankForm.controls['transactionid'].setValue(this.transDetails.id)
    this.addItem();
    this.bankForm.patchValue({amount:this.totalAmt});
    if (window.confirm('Are  you sure you want to save this voucher?')) {
    if (this.bankForm.valid) {
      
      // this.bankForm.controls['transactionid'].setValue(this.transDetails.transactionid)
      // this.bankForm.controls['id'].setValue(this.transDetails.id)
      this.bankForm.patchValue({ "id": this.transDetails.id });
      this.model = this.bankForm.value;
      this.model.voucherinfo=this.items;
      // this.createItem(this.bankForm.value.id);
      this.RS.create(this.model).subscribe({
        next: (result: any) => {
          this.transDetails = undefined;
          this.toastr.success(result.message, 'Success');
          this.resetForm();
          //this.bankForm = this.fb.group(this.formLayout);
          this.items=[];
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
    this.srchForm.patchValue({'srch_term':""});
    this.bankForm.get("lgid")?.valueChanges.subscribe({next:(d)=>{
      this.getPalikaDetails();
      this.getBankAccounts();
    }});
    this.transDetails = undefined;
  }
  getRevenue(bankorgid:any){
    // const bankorgid=this.bankForm.value['bankorgid'];
    console.log(bankorgid);
    this.bvs.getRevenue(bankorgid).subscribe({next:(dt)=>{
      this.revs = dt.data;
    },error:err=>{

    }});

  }

  transDetails: any;
  istab = 1;
  search() {
    this.transDetails=undefined;
    this.items=new Array();
    if (this.srchForm.valid) {
      this.RS.getTranactionData(this.srchForm.value.srch_term).subscribe({
        next: (dt) => {
          this.transDetails = dt.data;
       
          this.items=this.transDetails.revs;
          this.calctotal();
          this.bankForm.patchValue({lgid:this.transDetails.lgid,'taxpayerpan':this.transDetails.taxpayerpan,'taxpayername':this.transDetails.taxpayername,'amount':this.transDetails.amount});
        
          if (this.transDetails.trantype == 1) {
            this.istab = 1;
          } else {
            this.istab = 2;
          }
         
          this.bankForm.patchValue({'collectioncenterid':this.transDetails.collectioncenterid,'accountno':this.transDetails.accountno});
        }, error: error => {
          // console.log(error);
          // alert(5)
          this.toastr.error(error.error.message, "Error");
        }
      });
    }
  }
  
  
  acs:any;
  getBankAccounts(){
    // this.acs  = undefined;
    const llgCode = this.bankForm.value['lgid'];
    if(llgCode){
      this.rs.getBankAccounts(llgCode).subscribe({
        next:(d)=>{
          this.acs = d.data;
          setTimeout(()=>{
            if(this.transDetails){
              this.bankForm.patchValue({"bankorgid":this.transDetails.bankorgid});
            }
          });
          this.getRevenue(this.transDetails.bankorgid);
        },error:err=>{
          // console.log(err);
        }
      });
    }
  }

  getUpdateItem(id: any) {
    this.RS.getEdit(id).subscribe({
      next:(result: any) => {
        this.model = result;
        this.bankForm.patchValue(result);
        this.changeFields();
      },error:(error: any) => {
        this.toastr.error(error.error, 'Error');
      }
  });
    }
}
