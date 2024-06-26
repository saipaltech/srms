import { Component, Input, OnInit, TemplateRef } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { DatePipe, DOCUMENT } from '@angular/common';
import { FormBuilder, FormGroup, Validators, FormControl } from '@angular/forms';
import { VoucherServiceOff } from './voucher.service';
import { ValidationService } from '../validation.service';

import { BsModalService, BsModalRef } from 'ngx-bootstrap/modal';

import { Router, NavigationExtras, Route } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { AppConfig } from '../app.config';
import { BankService } from '../bank/bank.service';



@Component({
  selector: 'app-voucher-bank-off',
  templateUrl: './voucher-bank.component.html',
  styleUrls: ['./voucher-bank.component.scss'],
  providers: [DatePipe]
})
export class VoucherBankOffComponent implements OnInit {

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
  approved ="0";
  isNote:boolean=false;
  items=new Array();
  items1=new Array();


constructor(private appconfig:AppConfig ,private datePipe: DatePipe,private bs:BankService, private toastr: ToastrService, private fb: FormBuilder,private bvs:VoucherServiceOff, private modalService: BsModalService, private r: Router,private auth:AuthService){
  
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
      ttype:['1'],
      cb:[''],
      district:[''],
      cb1:[''],
      note:[''],
      totalno:['',Validators.pattern('[0-9]+')],
      return:['0',Validators.pattern('[0-9]+')],
      directdeposit:['']
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


ngOnInit(): void {
  // console.log(this.items);
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
}

dist:any;
  getDistrict(){
    this.bs.getDistrict().subscribe({next:(d:any)=>{
      this.dist = d;
    },error:err=>{

    }});
  }

  getPalika(id:any){
    this.voucherBankForm.patchValue({lgid:''});
    this.bs.getPalika(id).subscribe({next:(d:any)=>{
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

getAndSetPanDetails() {
  if(this.voucherBankForm.get("taxpayerpan")?.value){
    this.bvs.getPanDetails(this.voucherBankForm.get("taxpayerpan")?.value).subscribe({
      next: (d) => {
        if (d.data) {
          this.voucherBankForm.patchValue({ "taxpayername": d.data.taxpayer, "depositedby": d.data.taxpayer, "depcontact": d.data.contactNo })
        }
      }
    });
  }
}

showSlip(lgid:any,karobar:any){
  window.open(this.appconfig.baseUrl+"taxpayer-voucher/report-generate?voucherno="+karobar+"&palika="+lgid, "_blank");
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
      // console.log(err);
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
        // console.log(err);
      }
    });
  }
  
}

altmsg(msg:any){
  if(msg=="Invalid Pattern."){
    return "Number only";
  }
  return msg;
}

mobile(msg:any){
  if(msg=="Invalid Pattern."){
    return "Number only";
  }
  return msg;
}

checkvalue1(isChecked1: boolean){
  if (isChecked1 == true) {
    this.isNote=true;

  }else{
    this.isNote=false;
    this.items1=new Array();
  }
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




  showSuccess() {
    // this.toastr.success('Hello world!', 'Toastr fun!');
  }

  delete(id:any){
    if (window.confirm('Are sure you want to delete this item?')) {
      this.bvs.remove(id).subscribe({next:(result: any) => {
        this.toastr.success('Item Successfully Deleted!', 'Success');
        this.getList();
      },error:error => {
        this.toastr.error(error.error, 'Error');
      }});
    }
  }
  

  resetForm(){
    this.acs = undefined;
    this.items = new Array();
    this.items1 = new Array();
    this.isNote==false;
    this.checks=false;
    this.voucherBankForm =this.fb.group(this.formLayout);
    this.voucherBankForm.get("lgid")?.valueChanges.subscribe({next:(d)=>{
      this.getPalikaDetails();
      this.getBankAccounts();
    }});
    // this.voucherBankForm.patchValue({'lgid':this.dlgid});
    // this.items=new Array();
   
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

  setStatus(val: any) {
    this.approved = val.target.value;
    this.getList();
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
  else if(!this.voucherBankForm.value['revenuecode']){
    if(this.items.length<=0){
      this.toastr.error('Please fill all the राजस्व शिर्षक field', 'Error');
      return;
     } 
  }

  this.addItem();
    this.addItem1();
    if(this.isNote==true){
      if(this.totalAmt!=this.totalAmt1){
        this.toastr.error('Total amount and notes collection mismatched', 'Error');
        return;
      }
    }
   
  if (window.confirm('Are  you sure you want to save this voucher?')) {
    const dbd=this.voucherBankForm.value['directdeposit'];
  // this.voucherBankForm.patchValue({amount:this.totalAmt});
  if (this.voucherBankForm.valid) {   
    this.model = this.voucherBankForm.value;
    this.model.voucherinfo=this.items;
    this.model.amount=this.totalAmt;
    this.model.noteinfo=this.items1;
    if(dbd==true){
      this.model.directdeposit=1;
    }else{
      this.model.directdeposit=0;
    }
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


selectedRevenue:any;
addItem(){
  //  console.log(this.rv);
  
   let rc=this.voucherBankForm.value['revenuecode'];
   let amt=this.voucherBankForm.value['amount'];
   const hasCertainValue = this.items.some(item => item.rc === rc);
    if (hasCertainValue) {
     alert("same revenue title cannot be added ");
      return;
    }
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
   this.numberInWords="";
  
   }
   
  
  }

  addItem1() {
    //  console.log(this.rv);

    let rc = this.voucherBankForm.value['note'];
    //  console.log(rc)
    let amt = this.voucherBankForm.value['totalno'];

    //  if(amt)
    if (amt && rc && this.voucherBankForm.get('totalno')?.valid) {
      let val =rc*amt;
      // for (const item of this.revs1) {
      //   if (item.code === rc) {
      //     val = item.code + '[' + item.name + ']';
      //     // console.log(`Found key-value pair: ${item.key} : ${item.value}`);
      //     break;
      //   }
      // }

      var newItem = {
        nt: rc,
        no: amt,
        rv: val
      };

      // Add the new item to the items array
      this.items1.push(newItem);
      this.calctotal1();

      this.voucherBankForm.patchValue({ "note": '' });
      this.voucherBankForm.patchValue({ "totalno": '' });


    }


  }
  totalAmt=0;
  totalAmt1=0;
  calctotal(){
    this.totalAmt=0;
    for(const item of this.items){
      this.totalAmt+=parseFloat(item.amt);
   
    }
    this.totalAmt=parseFloat(this.totalAmt.toFixed(2));
  }
  

removeItem(index:any) {
 
  this.items.splice(index, 1);
  this.calctotal();
}

removeItem1(index: any) {
  this.items1.splice(index, 1);
  this.calctotal1();
}

getTotalAmt(){
  let amt = this.voucherBankForm.value['return'];
  this.totalAmt1=this.totalAmt1-amt;
}

calctotal1() {
  this.totalAmt1 = 0;
  for (const item of this.items1) {
    this.totalAmt1 += parseInt(item.rv);

  }
  let amt = this.voucherBankForm.value['return'];
  this.totalAmt1=this.totalAmt1-amt;
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
      this.resetForm();
      this.isNote=false;
      this.getList();
      let ks=result.data.karobarsanket;
      upd.approved=result.data.approved;
      window.open(this.appconfig.baseUrl+"taxpayer-voucher/report-generate?voucherno="+ ks + '&palika=' + upd.lgid, '_blank')          
      // window.open("/#/report-generate?voucherno="+ks+'&palika='+upd.lgid, '_blank')
    }, error:err => {
      this.toastr.error(err.error.message, 'Error');
    }
    });
  }

}

}

