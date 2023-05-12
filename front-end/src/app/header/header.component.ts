import { Component, OnInit } from '@angular/core';
import { AuthService } from '../auth/auth.service';
import { ChequeEntryService } from '../cheque-entry/cheque-entry.service';


@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit{

  branch = ""
  bank = ""

  constructor(private auth: AuthService,private bvs:ChequeEntryService) {
    const details = auth.getUserDetails();
    if (details) {
      this.bank = details.bank;
      this.branch = details.branch;
    }
  }

  usertype:any
  ngOnInit(): void {
    // alert("hh");
    this.bvs.getUsertype().subscribe({next:(dt)=>{
      this.usertype = dt.data;
      console.log(this.usertype);
      // this.voucherBankForm.patchValue({"lgid":this.dlgid});
    },error:err=>{

    }});
  }


}
