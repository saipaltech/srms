import { Component, Input, OnInit, TemplateRef } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { DatePipe, DOCUMENT } from '@angular/common';
import { FormBuilder, FormGroup, Validators, FormControl } from '@angular/forms';
import { ValidationService } from '../validation.service';
import { BsModalService, BsModalRef } from 'ngx-bootstrap/modal';
import { Router, NavigationExtras, Route } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { ChequeEntryService } from './cheque-entry.service';
import { ltLocale } from 'ngx-bootstrap/chronos';
import { AppConfig } from '../app.config';
import { BankService } from '../bank/bank.service';



@Component({
  selector: 'app-cheque-entry',
  templateUrl: './cheque-entry.component.html',
  styleUrls: ['./cheque-entry.component.scss'],
  providers: [DatePipe]
})
export class ChequeEntryComponent implements OnInit {

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
  items=new Array();

constructor(private appconfig:AppConfig,private datePipe: DatePipe,private bs:BankService, private toastr: ToastrService, private fb: FormBuilder,private bvs:ChequeEntryService, private modalService: BsModalService, private r: Router,private auth:AuthService){
  const ud = this.auth.getUserDetails();
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
      bankorgid:['',Validators.required],
      revenuecode: [''],
      purpose: [''],
      amount:[''],
      chequeamount:['',[Validators.required]],
      chequeno:['',Validators.required],
      chequebank:['',Validators.required],
      ttype:['2'],
      chequetype:['',Validators.required],
      cb:[''],
      district:[''],
      directdeposit:['0']
    }
    this.voucherBankForm =fb.group(this.formLayout)
    this.srchForm = new FormGroup({
      entries: new FormControl('10'),
      srch_term: new FormControl('')})
}

openModal(template: TemplateRef<any>, id:any,cstatus:any) {
  this.modalRef = this.modalService.show(template);
  this.getDetails(id);
  if(cstatus==0){
    this.hideApproveButton= true;
  }else{
    this.hideApproveButton= false;
  }

}

formvalue=true; 

clearCheque(id:any){
  this.getDetails(id);
  // console.log(this.details);
  if (window.confirm('Are you sure this cheque is cleared?')) {
    this.getDetails(id);
  this.bvs.clearCheque(id).subscribe({next:(dt)=>{
    // this.getDetails(id);
    // console.log(dt);
    window.open(this.appconfig.baseUrl+"taxpayer-voucher/report-generate?voucherno="+ this.details.karobarsanket + '&palika=' + this.details.lgid, '_blank'); 
   
    this.getList();
    this.toastr.success("Cheque status changed to cleared.","Success")
    // window.open("/#/cheque-report?voucherno="+this.details.karobarsanket+'&palika='+this.details.lgid +'&formvalue='+this.formvalue, '_blank');
    this.modalRef?.hide();
  },error:err=>{
    // console.log(err);
    this.toastr.error(err.message,"Error")
  }});
}
}

details : any;

getDetails(id:any){
  this.bvs.getDetails(id).subscribe({next:(dt)=>{
    this.details = dt;
    // console.log(this.details);
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
  //console.log(this.items);
  this.pagination.perPage = this.perPages[0];
  this.getList();
  this.getDistrict();
  this.voucherBankForm.get("lgid")?.valueChanges.subscribe({next:(d)=>{
    this.getPalikaDetails();
    this.getBankAccounts();
  }});
  this.bvs.getLocalLevels().subscribe({next:(dt)=>{
      this.llgs = dt.data;
      // this.voucherBankForm.patchValue({"lgid":this.dlgid});
    },error:err=>{

    }});

    // this.bvs.getRevenue().subscribe({next:(dt)=>{
    //   this.revs = dt.data;
    // },error:err=>{

    // }});
    this.getBank();
}

dist:any;
  getDistrict(){
    this.bs.getDistrict().subscribe({next:(d:any)=>{
      this.dist = d;
    },error:err=>{

    }});
  }

  getPalika(id:any){
    this.bs.getPalikaAll(id).subscribe({next:(d:any)=>{
      this.llgs = d;
    },error:err=>{

    }});
  }

getRevenue(){
  const bankorgid=this.voucherBankForm.value["bankorgid"];
   this.bvs.getRevenue(bankorgid).subscribe({next:(dt)=>{
      this.revs = dt.data;
    },error:err=>{

    }});

}

getUpdateItem(id: any) {
  this.bvs.getEdit(id).subscribe(
    (result: any) => {
      this.model = result;
      this.voucherBankForm.patchValue(result);
      this.changeFields();
      this.getRevenue();
    },
    (error: any) => {
      this.toastr.error(error.error, 'Error');
    }
  );
}
banks:any;
getBank(){
 
   this.bvs.getBank().subscribe({next:(dt)=>{
      this.banks = dt.data;
    },error:err=>{

    }});

}

getAndSetPanDetails(){
  this.bvs.getPanDetails(this.voucherBankForm.get("taxpayerpan")?.value).subscribe({next:(d)=>{
    if(d.data){
      this.voucherBankForm.patchValue({"taxpayername":d.data.taxpayer,"depositedby":d.data.taxpayer,"depcontact":d.data.contactNo})
    }
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
      //console.log(err);
    }
  });
}
rv:any;
getBankAccounts(){
  this.acs  = undefined;
  const llgCode = this.voucherBankForm.value['lgid'];
  if(llgCode){
    this.bvs.getBankAccounts(llgCode).subscribe({
      next:(d)=>{
        this.acs = d.data;
        if(d.data.length==1){
          this.voucherBankForm.patchValue({"bankorgid":d.data[0].id});
        }
      },error:err=>{
        //console.log(err);
      }
    });
  }
  
}




  showSuccess() {
    // this.toastr.success('Hello world!', 'Toastr fun!');
  }
  

  resetForm(){
    this.acs = undefined;
    this.voucherBankForm =this.fb.group(this.formLayout);
    this.voucherBankForm.get("lgid")?.valueChanges.subscribe({next:(d)=>{
      this.getPalikaDetails();
      this.getBankAccounts();
    }});
    // this.voucherBankForm.patchValue({'lgid':this.dlgid});
    this.items=new Array();
    this.istab=1;
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

  numberInWords:any;
  convertToWords(value: any) {
    const ones = ['', 'one', 'two', 'three', 'four', 'five', 'six', 'seven', 'eight', 'nine'];
    const teens = ['ten', 'eleven', 'twelve', 'thirteen', 'fourteen', 'fifteen', 'sixteen', 'seventeen', 'eighteen', 'nineteen'];
    const tens = ['', 'ten', 'twenty', 'thirty', 'forty', 'fifty', 'sixty', 'seventy', 'eighty', 'ninety'];
    const scales = ['', 'thousand', 'million', 'billion'];
  
    if (value === 0) {
      this.numberInWords = 'zero';
      return;
    }
  
    const numStr = value.toString();
    const [integerPart, decimalPart] = numStr.split('.');
  
    let result = '';
    if (integerPart) {
      let remainingValue = parseInt(integerPart);
      let scaleIndex = 0;
  
      while (remainingValue > 0) {
        const currentGroup = remainingValue % 1000;
        remainingValue = Math.floor(remainingValue / 1000);
  
        if (currentGroup !== 0) {
          result = this.convertGroupToWords(currentGroup, ones, teens, tens) + ' ' + scales[scaleIndex] + ' ' + result;
        }
  
        scaleIndex++;
      }
    }
  
    if (decimalPart) {
      const decimalValue = parseInt(decimalPart);
      if (decimalValue !== 0) {
        result += 'and ' + this.convertGroupToWords(decimalValue, ones, teens, tens)+' paisa';
      }
    }
  
    this.numberInWords = "Rupees "+result.trim();
  }
  
  convertGroupToWords(group: number, ones: string[], teens: string[], tens: string[]): string {
    let result = '';
  
    const hundreds = Math.floor(group / 100);
    const tensUnits = group % 100;
  
    if (hundreds > 0) {
      result += ones[hundreds] + ' hundred';
      if (tensUnits > 0) {
        result += ' ';
      }
    }
  
    if (tensUnits > 0) {
      if (tensUnits < 10) {
        result += ones[tensUnits];
      } else if (tensUnits < 20) {
        result += teens[tensUnits - 10];
      } else {
        const tensDigit = Math.floor(tensUnits / 10);
        const unitsDigit = tensUnits % 10;
        result += tens[tensDigit];
        if (unitsDigit > 0) {
          result += ' ' + ones[unitsDigit];
        }
      }
    }
  
    return result.trim();
  }
  
  

   
  

voucherBankFormSubmit(){
  if (!this.voucherBankForm.value['collectioncenterid']){
    this.toastr.error('Please fill the संकलन केन्द्र field', 'Error');
    return;
  }
  else if(!this.voucherBankForm.value['lgid']){
    this.toastr.error('Please fill the पालिका field', 'Error');
    return;
  }
  // else if(!this.voucherBankForm.value['revenuecode']){
  //   if(this.items.length<=0){
  //     this.toastr.error('Please fill all the राजस्व शिर्षक field', 'Error');
  //     return;
  //    } 
  // }
   
  if (window.confirm('Are  you sure you want to save this voucher?')) {
  this.addItem();
  // this.voucherBankForm.patchValue({amount:this.totalAmt});
  const camt=this.voucherBankForm.value['chequeamount'];
  if(camt!=this.totalAmt){
    this.toastr.error('Cheque Amount and total amount should be equal.', 'Error');
    return;
  }
  if (this.voucherBankForm.valid) {
    const llgCode = this.voucherBankForm.value['lgid'];
    if(llgCode!=this.dlgid){
      if(!confirm("You have selected Local Level other then default, Are you sure to proceed")){
        return;
      }
    }
    this.model = this.voucherBankForm.value;
    this.model.voucherinfo=this.items;
    this.model.amount=this.totalAmt;
    this.model.directdeposit=0;
    this.createItem(this.voucherBankForm.value.id);
    // alert('submit but not create')
    // console.log(this.voucherBankForm.value)

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

istab=1;
selectedRevenue:any;
addItem(){
  //  console.log(this.rv);
  
   let rc=this.voucherBankForm.value['revenuecode'];
   let amt=this.voucherBankForm.value['amount'];
   if(amt && rc && this.voucherBankForm.get('amount')?.valid){
    let val;
    for (const item of this.revs) {
     if (item.id === rc) {
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
   this.voucherBankForm.patchValue({"revenuecode":''});
   this.voucherBankForm.patchValue({"amount":''});
   this.istab=2;
   this.numberInWords="";
   }
   
  
  }
  totalAmt=0;
  calctotal(){
    this.totalAmt=0;
    // console.log(this.items.length);
    for(const item of this.items){
      // console.log(item);
      this.totalAmt+=parseFloat(item.amt);
   
    }
    this.totalAmt=parseFloat(this.totalAmt.toFixed(2));
  }

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

  checks=false;
  checkvalue(isChecked: boolean) {

    if (isChecked == true) {
      this.checks=true;
      this.voucherBankForm.patchValue({ 'depositedby': this.voucherBankForm.value['taxpayername'] });

    } else {
      this.checks=false;
      this.voucherBankForm.patchValue({ 'depositedby': "" });

    }
  }

  setPayerName(){
    if(this.checks==true){
      this.voucherBankForm.patchValue({ 'depositedby': this.voucherBankForm.value['taxpayername'] });
    }
  }
  

removeItem(index:any) {
 
  this.items.splice(index, 1);
  this.calctotal();
}

removeItems(index:any) {
  // this.calctotal();
  this.items.splice(index, 1);
}
delete(id:any){
  if (window.confirm('Are sure you want to delete this item?')) {
    this.bvs.remove(id).subscribe((result: any) => {
      this.toastr.success('Item Successfully Deleted!', 'Success');
      this.getList();
    }, (error: { error: any; }) => {
      this.toastr.error(error.error.message, 'Error');
    });
  }
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
      this.toastr.success('Item Successfully Saved!', 'Success');
      this.resetForm();
      this.getList();  
      this.checks=false;
      let ks=result.data.karobarsanket;
      let ct=result.data.chequetype;
      let formValue=false;
      if(ct=="2"){
        formValue=true;
      }else{
        formValue=false;
      }
      window.open(this.appconfig.baseUrl+"taxpayer-voucher/report-generate?voucherno="+ ks + '&palika=' + upd.lgid, '_blank');         
      // this.istab=1;
      // window.open("/#/cheque-report?voucherno="+ks+'&palika='+upd.lgid+'&formvalue='+formValue, '_blank');
      // window.open("/#/report-generate?voucherno="+upd.voucherno+'&palika='+upd.lgid, '_blank')

    }, error:err => {
      this.toastr.error(err.error.message, 'Error');
    }
    });
  }

}
showSlip(lgid:any,karobar:any){
  window.open(this.appconfig.baseUrl+"taxpayer-voucher/report-generate?voucherno="+karobar+"&palika="+lgid, "_blank");
}

}

