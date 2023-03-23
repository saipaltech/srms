import { Component } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { DatePipe } from '@angular/common';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-voucher-bank',
  templateUrl: './voucher-bank.component.html',
  styleUrls: ['./voucher-bank.component.scss'],
  providers: [DatePipe]
})
export class VoucherBankComponent {
  myDate: any = new Date();

  voucherBankForm!: FormGroup;
  formLayout: any;

constructor(private datePipe: DatePipe, private toastr: ToastrService, private fb: FormBuilder){
    this.myDate = this.datePipe.transform(this.myDate, 'yyyy-MM-dd');
    this.formLayout = {
      id:[],
      usecase: ['',Validators.required],
      rajsahowsirsak: ['',Validators.required],
      jarinumber: ['',Validators.required],
      jariname: ['',Validators.required],
      kardataname: ['',Validators.required],
      panno: ['',Validators.required],
      amount: ['',Validators.required],
      accountno: ['',Validators.required],
      voucherno: ['',Validators.required],
      kendra: ['',Validators.required],
      palika: ['',Validators.required],
      date: [this.myDate,Validators.required]
      
    }
    
    this.voucherBankForm =fb.group(this.formLayout)
}

voucherBankFormSubmit(){

}

  showSuccess() {
    this.toastr.success('Hello world!', 'Toastr fun!');
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
