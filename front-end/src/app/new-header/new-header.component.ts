import { Component, Inject, OnInit } from '@angular/core';
import { AuthService } from '../auth/auth.service';
import { DOCUMENT } from '@angular/common';
import { Router } from '@angular/router';
import { ChequeEntryService } from '../cheque-entry/cheque-entry.service';
import { TranslateService } from '@ngx-translate/core';

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

  constructor(private auth: AuthService, @Inject(DOCUMENT) private document: Document, private router: Router,private bvs:ChequeEntryService, private translateService: TranslateService) {
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
    this.bvs.getUsertype().subscribe({next:(dt)=>{
      this.usertype = dt.usertype;
      // this.voucherBankForm.patchValue({"lgid":this.dlgid});
    },error:err=>{

    }});
    // const lang = localStorage.getItem("lang") || "np-Np";
    const lang = "en-EN";
    this.changeLang(lang);
  }

  changeLang(lan: string) {
    localStorage.setItem("lang",lan);
    this.translateService.use(lan);
  }
  
  logout() {
    if (confirm("Are you sure you want to logout?")) {
      this.auth.logout();
      this.router.navigate(['/login']);
    }
  }
  
  

}
