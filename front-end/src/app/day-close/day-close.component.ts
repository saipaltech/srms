import { DatePipe } from '@angular/common';
import { Component, QueryList, ViewChildren } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '../auth/auth.service';
import { ChequeEntryService } from '../cheque-entry/cheque-entry.service';
import { ValidationService } from '../validation.service';

@Component({
  selector: 'app-day-close',
  templateUrl: './day-close.component.html',
  styleUrls: ['./day-close.component.scss'],
  providers: [DatePipe]
})
export class DayCloseComponent {
  formLayout: any;
  formLayout1: any;
  llgs:any;
  vs = ValidationService;
  selectedCar!: number;
  myDate: any = new Date();
    cars = [
        { id: 1, name: 'Volvo' },
        { id: 2, name: 'Saab' },
        { id: 3, name: 'Opel' },
        { id: 4, name: 'Audi' },
    ];
    
    
  voucherBankForm!: FormGroup;
  daycloseForm!: FormGroup;
  formBuilder: any;
  selectedval=new Array();
    constructor(private datePipe: DatePipe, private toastr: ToastrService, private fb: FormBuilder,private bvs:ChequeEntryService, private modalService: BsModalService, private r: Router,private auth:AuthService){
      const ud = this.auth.getUserDetails();
      
      this.myDate = this.datePipe.transform(this.myDate, 'yyyy-MM-dd');
        this.formLayout = {
          id:[],
          date: [this.myDate],
          acno: [''],
          lgid: [''],
          
          
        }
        this.formLayout1 = {
          date: [this.myDate],
          acno: ['',Validators.required],
          lgid: ['',Validators.required],
          corebankid:[''],
          options: this.fb.array([], [Validators.required])
          // lists: new FormControl([])
          
        }
        this.voucherBankForm =fb.group(this.formLayout);
        this.daycloseForm =fb.group(this.formLayout1);

       
       
      
    }
    

    ngOnInit(): void {
     
     
      this.bvs.getLocalLevels().subscribe({next:(dt)=>{
          this.llgs = dt.data;
          // this.voucherBankForm.patchValue({"lgid":this.dlgid});
        },error:err=>{

        }});
    
    }

    onCheckboxChange(e:any) {

      const options: FormArray = this.daycloseForm.get('options') as FormArray;
  
      if (e.target.checked) {
  
        options.push(new FormControl(e.target.value));
  
      } else {
  
         const index = options.controls.findIndex(x => x.value === e.target.value);
  
         options.removeAt(index);
  
      }
      console.log(options.value);
      this.selectedval=options.value;
    }
    
acs:any;
    getBankAccounts(){
      this.acs  = undefined;
      const llgCode = this.voucherBankForm.value['lgid'];
      if(llgCode){
        this.bvs.getBankAccounts(llgCode).subscribe({
          next:(d)=>{
            this.acs = d.data;
            if(d.data.length==1){
              this.voucherBankForm.patchValue({"accountno":d.data[0].acno});
            }
          },error:err=>{
            //console.log(err);
          }
        });
      }
      
    }
    model: any = {};
    lists:any;
    voucherBankFormSubmit(){
      this.lists=undefined;
      this.model = this.voucherBankForm.value;
      // console.log(this.model.acno);
      if (this.voucherBankForm.valid) {
      this.bvs.getdayclose(this.model).subscribe({
        next:(result:any) => {
          this.lists=result.data;
          this.daycloseForm.patchValue({'lgid':this.model.lgid,'acno':this.model.acno});
        
        // this.toastr.success('Item Successfully Saved!', 'Success');
        // this.resetForm();
        // this.getList();
      
      }, error:err => {
        this.toastr.error(err.error.message, 'Error');
      }
      });
    }else{
      Object.keys(this.voucherBankForm.controls).forEach(field => {
        const singleFormControl = this.voucherBankForm.get(field);
        singleFormControl?.markAsTouched({onlySelf: true});
      });
    }
    }
    model1:any;
    daycloseFormSubmit(){
     
      // console.log(this.check);
      if(this.check==false){
        alert("Missing tick");
        return;
      }
      if (window.confirm('Are  you sure you want to save ?')) {
      this.model1 = this.daycloseForm.value;
      this.model1.selection=this.selectedval;
      this.bvs.submitdayclose(this.model1).subscribe({
        next:(result:any) => {
          // this.lists=result.data;
        
        this.toastr.success('Item Successfully Saved!', 'Success');
        this.resetForm();
        this.daycloseForm.value['options']="";
        this.selectedval=new Array();
        // this.getList();
      
      }, error:err => {
        // console.log(err.error);
        this.toastr.error(err.error.message, 'Error');
      }
      });
    }
    }

    resetForm(){
      this.lists = undefined;
      this.voucherBankForm =this.fb.group(this.formLayout);
      this.daycloseForm =this.fb.group(this.formLayout1);
     
    }
check=false;
    checkvalue(isChecked: boolean){
  
      if (isChecked==true) {
        this.check=true;
        // this.voucherBankForm.patchValue({'depositedby': this.voucherBankForm.value['taxpayername']});
     
      } else {
        this.check=false;
        // this.voucherBankForm.patchValue({'depositedby': ""});
       
      }
    }

}
