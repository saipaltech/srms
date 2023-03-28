import { Component, OnInit } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { DatePipe } from '@angular/common';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { VoucherService } from './voucher.service';

@Component({
  selector: 'app-voucher-bank',
  templateUrl: './voucher-bank.component.html',
  styleUrls: ['./voucher-bank.component.scss'],
  providers: [DatePipe]
})
export class VoucherBankComponent implements OnInit {
  myDate: any = new Date();

  voucherBankForm!: FormGroup;
  formLayout: any;
  llgs:any;
  ccs:any;
  acs:any;
  revs:any;

constructor(private datePipe: DatePipe, private toastr: ToastrService, private fb: FormBuilder,private bvs:VoucherService){
    this.myDate = this.datePipe.transform(this.myDate, 'yyyy-MM-dd');
    this.formLayout = {
      id:[],
      date: [this.myDate,Validators.required],
      voucherno: ['',Validators.required],
      taxpayername: ['',Validators.required],
      taxpayerpan: [''],
      depositedby:['',Validators.required],
      depcontact: ['',Validators.required],
      llgcode: ['',Validators.required],
      llgname: ['',Validators.required],
      costcentercode: ['',Validators.required],
      costcentername: ['',Validators.required],
      accountno:['',Validators.required],
      revenuecode: ['',Validators.required],
      revenuetitle: ['',Validators.required],
      purpose: [''],
      amount:['',Validators.required]
    }
    
    this.voucherBankForm =fb.group(this.formLayout)
}

ngOnInit(): void {
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
  const llgCode = this.voucherBankForm.value['llgcode'];
  this.bvs.getPlaikaDetails(llgCode).subscribe({
    next:(d)=>{
      this.acs = d.bankacs;
      this.ccs = d.costcentres;
    },error:err=>{
      console.log(err);
    }
  });
}

voucherBankFormSubmit(){

}

  showSuccess() {
    // this.toastr.success('Hello world!', 'Toastr fun!');
  }
  
  selectedCar!: number;

  cars = [
      { id: 1, name: 'Volvo' },
      { id: 2, name: 'Saab' },
      { id: 3, name: 'Opel' },
      { id: 4, name: 'Audi' },
  ];

  selectedCars!: number;

  car = [
      { id: 1, name: 'Volvo' },
      { id: 2, name: 'Saab' },
      { id: 3, name: 'Opel' },
      { id: 4, name: 'Audi' },
  ];

  resetForm(){
    this.voucherBankForm =this.fb.group(this.formLayout);
  }
}
