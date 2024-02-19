import { DatePipe } from '@angular/common';
import { Component, ElementRef, EventEmitter, QueryList, Renderer2, ViewChild, ViewChildren } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { ToastrService } from 'ngx-toastr';
import { AppConfig } from '../app.config';
import { AuthService } from '../auth/auth.service';
import { ChequeEntryService } from '../cheque-entry/cheque-entry.service';
import { ValidationService } from '../validation.service';

@Component({
  selector: 'app-verify-direct-deposit',
  templateUrl: './verify-direct-deposit.component.html',
  styleUrls: ['./verify-direct-deposit.component.scss'],
  providers: [DatePipe]
})
export class VerifyDirectBankDepositComponent {
  @ViewChildren('checkbox')
  checkboxes!: QueryList<ElementRef>;
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
    checkboxChangeEvent = new EventEmitter<any>();
  // textboxes: FormArray ;
  slctAll = false;
  voucherBankForm!: FormGroup;
  daycloseForm!: FormGroup;
  formBuilder: any;
  perPages = [10, 20, 50, 100];
  pagination = {
    total: 0,
    currentPage: 0,
    perPage: 10
  };
  searchTerm: string = '';
  column: string = '';
  isDesc: boolean = false;
  srchForm!: FormGroup;
  selectedval=new Array();
    constructor(private renderer: Renderer2,private appconfig:AppConfig,private datePipe: DatePipe, private toastr: ToastrService, private fb: FormBuilder,private bvs:ChequeEntryService, private modalService: BsModalService, private r: Router,private auth:AuthService){
      const ud = this.auth.getUserDetails();
      
      this.myDate = this.datePipe.transform(this.myDate, 'yyyy-MM-dd');
        this.formLayout = {
          id:[],
          sksno: ['',Validators.required],
          bksno: ['',Validators.required],
          remarks: [''],
          
          
        }
        this.srchForm = new FormGroup({
          entries: new FormControl('10'),
          srch_term: new FormControl('')})
      
        
        this.formLayout1 = {
          date: [this.myDate],
          acno: ['',Validators.required],
          lgid: ['',Validators.required],
          corebank:this.fb.group({}),
          // textboxes: this.fb.array([]),
          options: this.fb.array([], [Validators.required])
          // lists: new FormControl([])
          
        }
        this.voucherBankForm =fb.group(this.formLayout);
        this.daycloseForm =fb.group(this.formLayout1);
        // this.textboxes = this.daycloseForm.get('textboxes') as FormArray;
       
       
      
    }
    

    ngOnInit(): void {
      this.pagination.perPage = this.perPages[0];
     this.getList();
     
      // this.bvs.getLocalLevels().subscribe({next:(dt)=>{
      //     this.llgs = dt.data;
      //     // this.voucherBankForm.patchValue({"lgid":this.dlgid});
      //   },error:err=>{

      //   }});
    
    }
    bdetails:any;
    sdetails:any;
    getSutraDetails(ksno:any){
      this.sdetails=undefined;
      this.bvs.getDetailsSutra(ksno).subscribe({
        next: (dt) => {
            this.sdetails = JSON.parse(dt.data);
          
          console.log(this.sdetails);
        }, error: err => {
          this.toastr.error("Unable to Fetch Data", "Error")
        }
      });
    }
    getOwnDetails(ksno:any){
      this.bdetails=undefined;
      this.bvs.getDetailsOwn(ksno).subscribe({
        next: (dt) => {
          this.bdetails = dt;
          console.log(this.bdetails);
        }, error: err => {
          this.toastr.error("Unable to Fetch Data", "Error")
        }
      });
    }

    submitToPalika(id:any){
      if (window.confirm('Are you sure you want to submit to palika?')) {
        this.bvs.submitToPalika(id).subscribe({
          next: (dt) => {
            this.toastr.success("Data submitted successfully!", "Success");
            this.getList();
          }, error: err => {
            this.toastr.error("Unable to submit data", "Error")
          }
        });
      }
    }

    delete(id:any){
      if (window.confirm('Are you sure you want to delete this item?')) {
        this.bvs.deleteVoucher(id).subscribe({
          next: (dt) => {
            this.toastr.success("Data deleted successfully!", "Success");
            this.getList();
          }, error: err => {
            this.toastr.error("Unable to delete data", "Error")
          }
        });
      }
    }

    getList(pageno?: number | undefined) {
      const page = pageno || 1;
      this.bvs.getListDirectdeposit(this.pagination.perPage, page, this.searchTerm, this.column, this.isDesc).subscribe(
        (result: any) => {
          this.lists = result.data;
          this.pagination.total = result.total;
          this.pagination.currentPage = result.currentPage;
          // console.log(result);
        },
        error => {
           this.toastr.error(error.error);
        }
      );
    }

    paginatedData($event: { page: number | undefined; }) {
      this.getList($event.page);
    }
    
    changePerPage(perPage: number) {
      this.pagination.perPage = perPage;
      this.pagination.currentPage = 1;
      this.getList();
    }
   
    onCheckboxChange(e:any,item:any) {
      // console.log(item);
      const isChecked = e.target.checked;
      // this.checkboxChangeEvent.emit({ isChecked, item });
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

    search(){

    }
    model: any = {};
    lists:any;
    voucherBankFormSubmit(){
      if(this.sdetails && this.bdetails){
      this.model = this.voucherBankForm.value;
      this.model.sutralgid=this.sdetails.lgid;
      this.model.banklgid=this.bdetails.lgid;
      this.model.sutraccid=this.sdetails.collectioncenterid;
      this.model.bankccid=this.bdetails.collectioncenterid;
      this.model.sutraamount=this.sdetails.amount;
      this.model.bankamount=this.bdetails.amount;
        if(this.model.sutralgid!=this.model.banklgid){
          this.toastr.error("Local Level not matched", 'Error');
          return;
        }
      

        if(this.model.sutraamount!=this.model.bankamount){
          this.toastr.error("Amount not matched", 'Error');
          return;
        }

      // console.log(this.model.acno);
      if (this.voucherBankForm.valid) {
      this.bvs.vouchercancel(this.model).subscribe({
        next:(result:any) => {
          this.toastr.success('Item Successfully Saved!', 'Success');
          this.resetForm();
          this.sdetails=undefined;
          this.bdetails=undefined;
        this.getList();
      
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
  }else{
    this.toastr.error("Invalid karobarsanket", 'Error');
  }
    }
    model1:any;
    daycloseFormSubmit(){
      // this.textboxes.push(new FormControl(''));
      // console.log(this.check);
      if(this.check==false){
        alert("Please confirm your submission!");
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

    selectAll(e:any){
      this.daycloseForm.value['options']="";
      this.selectedval=new Array();
      if(e.target.checked==true){
        this.checkboxes.forEach(checkbox => {
          const checkboxElem = checkbox.nativeElement as HTMLInputElement;
          checkboxElem.checked = true;
          this.renderer.setProperty(checkboxElem, 'checked', false);
          this.renderer
            .selectRootElement(checkboxElem)
            .dispatchEvent(new Event('change'));
        });

        this.checkboxes.forEach(checkbox => {
          const checkboxElem = checkbox.nativeElement as HTMLInputElement;
          checkboxElem.checked = true;
          this.renderer.setProperty(checkboxElem, 'checked', true);
          this.renderer
            .selectRootElement(checkboxElem)
            .dispatchEvent(new Event('change'));
        });
      }else{
        this.checkboxes.forEach(checkbox => {
          const checkboxElem = checkbox.nativeElement as HTMLInputElement;
          checkboxElem.checked = true;
          this.renderer.setProperty(checkboxElem, 'checked', false);
          this.renderer
            .selectRootElement(checkboxElem)
            .dispatchEvent(new Event('change'));
        });
       
      }
      
     
      // this.slctAll = true;
     
    }

    viewdayclose(lgid:any,acno:any,bankid:any,branchid:any){
      window.open(this.appconfig.baseUrl+"taxpayer-voucher/dayclose-details?lgid="+ lgid + '&bankorgid=' + acno+'&bankid='+bankid+'&branchid='+branchid, '_blank')
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
