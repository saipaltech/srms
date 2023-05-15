import { Component, Inject, OnInit } from '@angular/core';
import { AuthService } from '../auth/auth.service';
import { DOCUMENT } from '@angular/common';
import { Router } from '@angular/router';
import { ChequeEntryService } from '../cheque-entry/cheque-entry.service';
import { AppConfig } from '../app.config';

@Component({
  selector: 'app-new-header',
  templateUrl: './new-header.component.html',
  styleUrls: ['./new-header.component.scss']
})
export class NewHeaderComponent implements OnInit{
  
  branch = "";
  bank = "";
  username="";
  name="";

  constructor(private auth: AuthService, @Inject(DOCUMENT) private document: Document, private router: Router,private bvs:ChequeEntryService,public appConfig:AppConfig) {
    const details = auth.getUserDetails();
    if (details) {
      this.bank = details.bank;
      this.branch = details.branch;
      this.username = details.username;
      this.name = details.name;
    }
  }


  toggleSidebar() {
    const kl = "toggle-sidebar";
    // console.log('SideBar')
    if (this.document.body.classList.contains(kl)) {
      this.document.body.classList.remove(kl);
    } else {
      this.document.body.classList.add(kl);
    }
  }

  usertype:any
  ngOnInit(): void {
    // alert("hh");
    this.bvs.getUsertype().subscribe({next:(dt)=>{
      // console.log(dt.usertype);
      this.usertype = dt.usertype;
      // console.log(this.usertype);
      // this.voucherBankForm.patchValue({"lgid":this.dlgid});
    },error:err=>{

    }});
  }

  
  logout() {
    if (confirm("Are you sure you want to logout?")) {
      this.auth.logout();
      this.router.navigate(['/login']);
    }
  }
  
  

}
